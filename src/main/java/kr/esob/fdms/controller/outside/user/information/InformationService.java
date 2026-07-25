package kr.esob.fdms.controller.outside.user.information;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.seed.PasswordUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class InformationService implements CommonService {

	@Inject
	InformationDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public InformationListParam selectInformationDetailInfo(InformationListParam param) {
		InformationListParam vo = dao.selectInformationDetail(param);
		return vo;
	}

	public ResultVO insertRequest(InformationListParam param, Authentication authentication) throws Exception {
		ResultVO resultVo = new ResultVO();
		if (param == null) {
			resultVo.setMessage("msg.invalidRequest");
			return resultVo;
		}

		try {
			if (authentication == null || !(authentication.getPrincipal() instanceof UserVO)) {
				resultVo.setMessage("msg.accessDenied");
				return resultVo;
			}
			param.setSessionUser((UserVO) authentication.getPrincipal());

			if (!isSupportedRequestType(param.getRequestType())) {
				resultVo.setMessage("msg.invalidRequest");
				return resultVo;
			}

			String approvalUserCd = dao.selectCompanyApprover(param);
			if (!hasText(approvalUserCd)) {
				resultVo.setMessage("msg.approvalUserNotFound");
				return resultVo;
			}
			param.setApprovalUserCd(approvalUserCd);
			param.setProtectYn("Y".equals(param.getProtectYn()) ? "Y" : "N");
			param.setCrYn("Y".equals(param.getCrYn()) ? "Y" : "N");

			if ("I".equals(param.getRequestType())) {
				if (!PasswordUtils.isAcceptablePassword(param.getUserPwd())) {
					resultVo.setMessage("msg.invalidPassword");
					return resultVo;
				}
				param.setUserCd(null);
				param.setUserPwd(PasswordUtils.hashPasswordWithSalt(param.getUserPwd()));
			} else {
				if (!hasText(param.getUserCd()) || dao.selectCompanyUserCount(param) != 1) {
					resultVo.setMessage("msg.invalidRequest");
					return resultVo;
				}
				// Password changes are handled only by updateUser().
				param.setUserPwd(null);
			}

			dao.insertInfo(param);
			resultVo.setSuccess(true);
		} finally {
			// Do not retain either a raw password or its hash in a request DTO.
			param.setUserPwd(null);
		}
		return resultVo;
	}

	public ResultVO updateUser(InformationListParam param, Authentication authentication) throws Exception {
		ResultVO resultVo = new ResultVO();
		if (param == null || authentication == null
				|| !(authentication.getPrincipal() instanceof UserVO)) {
			resultVo.setMessage("msg.accessDenied");
			return resultVo;
		}

		UserVO userVo = (UserVO) authentication.getPrincipal();
		String currentPassword = param.getUserPwd();
		String newPassword = param.getUserNewPwd();

		try {
			if (!hasText(userVo.getUserCd()) || !hasText(currentPassword)
					|| !PasswordUtils.isAcceptablePassword(newPassword)) {
				resultVo.setMessage("msg.invalidPassword");
				return resultVo;
			}

			// The target identity and current hash are both resolved server-side.
			param.setUserCd(userVo.getUserCd());
			String storedPassword = dao.selectPasswordHash(param);
			if (!PasswordUtils.verifyPassword(storedPassword, currentPassword)) {
				resultVo.setMessage("msg.invalidPassword");
				return resultVo;
			}

			param.setUserPwd(null);
			param.setUserNewPwd(PasswordUtils.hashPasswordWithSalt(newPassword));
			if (dao.updateUser(param) == 1) {
				resultVo.setSuccess(true);
			} else {
				resultVo.setMessage("msg.error");
			}
		} finally {
			// Never leave credentials on a DTO that may later be logged or serialized.
			param.setUserPwd(null);
			param.setUserNewPwd(null);
		}

		return resultVo;
	}

	public ResultVO updateRequest(InformationListParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		dao.updateInfo(param);

		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO deleteRequest(InformationListParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		dao.deleteInfo(param);

		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO selectProtectCount(InformationListParam param)  throws Exception {
		ResultVO resultVo = new ResultVO();

		if (dao.selectProtectCount(param) == 0)
			resultVo.setSuccess(true);
		else
			resultVo.setSuccess(false);

		return resultVo;
	}

	public ResultVO selectCrCount(InformationListParam param)  throws Exception {
		ResultVO resultVo = new ResultVO();

		if (dao.selectCrCount(param) == 0)
			resultVo.setSuccess(true);
		else
			resultVo.setSuccess(false);

		return resultVo;
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private boolean isSupportedRequestType(String requestType) {
		return "I".equals(requestType) || "U".equals(requestType) || "D".equals(requestType);
	}

}
