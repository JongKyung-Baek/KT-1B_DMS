package kr.esob.fdms.controller.inside.organizationmanage.insideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.seed.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class InsideuserService implements CommonService {

	@Inject
	InsideuserDao dao;

	@Autowired
	DocPdfLinkRequestDao dao_for_pwd;


	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public ResultVO updateUnlock(UserVO userVo) {
		ResultVO resultVo = new ResultVO();
		dao.updateUnlock(userVo);
		resultVo.setSuccess(true);
		return resultVo;
	}

	public ResultVO resetPwd(UserVO userVo) {
		ResultVO resultVo = new ResultVO();

		List<Map<String,Object>> dbConfig = dao_for_pwd.selectDbConfig();
		String basicPassword = findBasicPassword(dbConfig);
		if (basicPassword == null || basicPassword.isEmpty()) {
			resultVo.setMessage("msg.error");
			resultVo.setSuccess(false);
			return resultVo;
		}

		String hashedPassword = PasswordUtils.hashPasswordWithSalt(basicPassword);
		userVo.setUserPwd(hashedPassword);

		dao.resetPwd(userVo);
		resultVo.setSuccess(true);
		return resultVo;
	}

	public UserListVO selectUser(String userCd) {
		return dao.selectUser(userCd);
	}


	@Transactional(rollbackFor = Exception.class)
	public ResultVO saveRegsiterUser(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
		String saveFlag = request.getParameter("saveFlag");
		String userCd = trim(request.getParameter("userCd"));
		String userId = trim(request.getParameter("userId"));
		String userNm = trim(request.getParameter("userNm"));
		String email = trim(request.getParameter("email"));
		String deptCd = trim(request.getParameter("deptCd"));
		String positionCd = trim(request.getParameter("positionCd"));
		String roleGroupCd = trim(request.getParameter("roleGroupCd"));

		if (!"I".equals(saveFlag) && !"U".equals(saveFlag)
				&& !"E".equals(saveFlag)) {
			resultVo.setMessage("msg.error");
			resultVo.setSuccess(false);
			return resultVo;
		}
		if (isBlank(userId) || isBlank(userNm) || isBlank(email)
				|| isBlank(deptCd) || isBlank(positionCd)
				|| isBlank(roleGroupCd)) {
			resultVo.setMessage("msg.selectValues");
			resultVo.setSuccess(false);
			return resultVo;
		}
		if (userId.length() > 20) {
			resultVo.setMessage("msg.invalidUserIdLength");
			resultVo.setSuccess(false);
			return resultVo;
		}
		if (userNm.length() > 256 || email.length() > 256) {
			resultVo.setMessage("msg.invalidInputLength");
			resultVo.setSuccess(false);
			return resultVo;
		}
		if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
			resultVo.setMessage("msg.invalidEmail");
			resultVo.setSuccess(false);
			return resultVo;
		}
		if (!"I".equals(saveFlag) && isBlank(userCd)) {
			resultVo.setMessage("msg.userNotFound");
			resultVo.setSuccess(false);
			return resultVo;
		}
//		String hashedPassword = PasswordUtils.hashPasswordWithSalt(request.getParameter("userPwd"));

		String useYn = "Y";
		String delYn = "N";
		String protectYn = "N";
		String lockYn = "N";
//		int distributionAuthCd = 0;

//		if (request.getParameter("newUser") != null && request.getParameter("newUser").equals("true")) {
//			newUser = true;
//		}

//		if(request.getParameter("distributionAuthCd") == null) distributionAuthCd = -1;
//		else distributionAuthCd = Integer.parseInt(request.getParameter("distributionAuthCd"));


		// userPopupParam 객체 만들어서 저장
		UserPopupParam userPopupParam = UserPopupParam.builder()
				.userId(userId)
				.userCd(userCd)
//				.userPwd(hashedPassword)
//				.businessAreaCd(request.getParameter("businessArea"))
				.userNm(userNm)
				.email(email)
				.deptCd(deptCd)
				.positionCd(positionCd)
				.roleGroupCd(roleGroupCd)
//				.authApprovalLevel(distributionAuthCd)
				.useYn(useYn)
				.delYn(delYn)
				.protectYn(protectYn)
				.lockYn(lockYn)
				.newUser(false)
				.build();

		if ("I".equals(saveFlag) || "U".equals(saveFlag)) {
			// Keep duplicate checks and the following insert/update atomic
			// across concurrent administrator requests.
			dao.lockUserMutation();
		}

		if ("I".equals(saveFlag)){ // 생성일때

			if (dao.checkUserId(userId) != null){
				resultVo.setMessage("msg.alrExistUser"); // "이미 존재하는 사용자 계정입니다"
				resultVo.setSuccess(false);
				return resultVo;
			} else if (dao.checkUserNm(userNm) != null) {
				resultVo.setMessage("msg.alrExistUserName"); // "이미 존재하는 사용자 이름입니다"
				resultVo.setSuccess(false);
				return resultVo;
			} else if(dao.checkEmail(email) > 0){
				resultVo.setMessage("msg.alrExistEmail"); // "이미 사용중인 이메일입니다"
				resultVo.setSuccess(false);
				return resultVo;
			} else {

				List<Map<String,Object>> dbConfig = dao_for_pwd.selectDbConfig();
				String basicPassword = findBasicPassword(dbConfig);
				if (basicPassword == null || basicPassword.isEmpty()) {
					resultVo.setMessage("msg.error");
					resultVo.setSuccess(false);
					return resultVo;
				}

				// 초기 비밀번호인 0000 삽입
				userPopupParam.setUserPwd(PasswordUtils.hashPasswordWithSalt(basicPassword));

				dao.insertRegisterUserInfo(userPopupParam); // 저장
				resultVo.setSuccess(true);
				return resultVo;
			}

		}else if ("U".equals(saveFlag)){ // 수정일때

			// 기존 사용자 정보 가져오기
			UserListVO userPopupParam_old = dao.getUserInfoByUserCd(userCd);

			// 기존 사용자 정보가 null 값이면 실패
			if (userPopupParam_old == null) {
				resultVo.setMessage("msg.userNotFound");
				resultVo.setSuccess(false);
				return resultVo;
			}


			// userId 체크. 변경됐으면, 변경값 db에서 체크. 이미 있으면 실패.
			if (!Objects.equals(userPopupParam_old.getUserId(), userId)) {
				if (dao.checkUserId(userId) != null) {
					resultVo.setMessage("msg.alrExistUser");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			// userNm 체크
			if (!Objects.equals(userPopupParam_old.getUserNm(), userNm)) {
				if (dao.checkUserNm(userNm) != null) {
					resultVo.setMessage("msg.alrExistUserName");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			// email 체크
			if (!Objects.equals(userPopupParam_old.getEmail(), email)) {
				if (dao.checkEmail(email) > 0) {
					resultVo.setMessage("msg.alrExistEmail");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}
			dao.editUserInfo(userPopupParam); // 정보 업데이트
			resultVo.setSuccess(true);
			return resultVo;
			} else if ("E".equals(saveFlag)){ // 수정일때. 여기에만 쓰이는 플래그

			// 기존 사용자 정보 가져오기
			UserListVO userPopupParam_old = dao.getUserInfoByUserCd(userCd);

			// 기존 사용자 정보가 null 값이면 실패
			if (userPopupParam_old == null) {
				resultVo.setMessage("msg.userNotFound");
				resultVo.setSuccess(false);
				return resultVo;
			}


			// userId 체크. 변경됐으면, 변경값 db에서 체크. 이미 있으면 실패.
			if (!Objects.equals(userPopupParam_old.getUserId(), userId)) {
				if (dao.checkUserId(userId) != null) {
					resultVo.setMessage("msg.alrExistUser");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			// userNm 체크
			if (!Objects.equals(userPopupParam_old.getUserNm(), userNm)) {
				if (dao.checkUserNm(userNm) != null) {
					resultVo.setMessage("msg.alrExistUserName");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			// email 체크
			if (!Objects.equals(userPopupParam_old.getEmail(), email)) {
				if (dao.checkEmail(email) > 0) {
					resultVo.setMessage("msg.alrExistEmail");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			String hashedPassword = PasswordUtils.hashPasswordWithSalt(request.getParameter("userPwd"));
			userPopupParam.setUserPwd(hashedPassword);

			dao.editUserInfo_resetPwd(userPopupParam); // 정보 업데이트
			resultVo.setSuccess(true);
			return resultVo;
		}

		resultVo.setSuccess(false);
		return resultVo;

		}

	private String findBasicPassword(List<Map<String, Object>> dbConfig) {
		if (dbConfig == null) {
			return null;
		}

		for (Map<String, Object> config : dbConfig) {
			if (config == null) {
				continue;
			}

			String configCd = strVal(config, "SYSTEM_CONFIG_CD", "system_config_cd");
			if (!"BASIC_PASSWORD".equals(configCd)) {
				continue;
			}

			return strVal(config, "SYSTEM_CONFIG_VALUE", "system_config_value");
		}

		return null;
	}

	private String strVal(Map<String, Object> config, String... keys) {
		for (String key : keys) {
			Object value = config.get(key);
			if (value != null) {
				return String.valueOf(value);
			}
		}

		return null;
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isEmpty();
	}

	}






