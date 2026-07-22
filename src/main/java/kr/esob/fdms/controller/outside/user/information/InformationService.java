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

	public ResultVO insertRequest(InformationListParam param) throws Exception {
		ResultVO resultVo = new ResultVO();
		dao.insertInfo(param);

		resultVo.setSuccess(true);
		return resultVo;
	}

	public boolean checkPwd(InformationListParam param) {

		if (dao.checkPwd(param) == 1)
			return true;
		else
			return false;
	}

	public ResultVO updateUser(InformationListParam param, Authentication authentication) throws Exception {
		ResultVO resultVo = new ResultVO();

		UserVO userVo = (UserVO) authentication.getPrincipal();
		String userPwd = userVo.getUserPwd();
		param.setUserPwd(userPwd);

		if (checkPwd(param)) {
			String hashedPassword = PasswordUtils.hashPasswordWithSalt(param.getUserNewPwd());
			param.setUserNewPwd(hashedPassword);
			dao.updateUser(param);
			resultVo.setSuccess(true);
		}
		else {
			resultVo.setSuccess(false);
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

}
