package kr.esob.fdms.controller.inside.organizationmanage.insideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.DateUtil;
import kr.esob.fdms.util.seed.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Service
public class InsideuserService implements CommonService {

	@Inject
	InsideuserDao dao;

	@Inject
	DateUtil dateUtil;

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


	public ResultVO saveRegsiterUser(MultipartHttpServletRequest request) throws Exception {
		ResultVO resultVo = new ResultVO();
//		String hashedPassword = PasswordUtils.hashPasswordWithSalt(request.getParameter("userPwd"));

		String authSite = "I";
		String useYn = "Y";
		String delYn = "N";
		String protectYn = "N";
		String lockYn = "N";
//		int distributionAuthCd = 0;
		boolean newUser = false;

//		if (request.getParameter("newUser") != null && request.getParameter("newUser").equals("true")) {
//			newUser = true;
//		}

//		if(request.getParameter("distributionAuthCd") == null) distributionAuthCd = -1;
//		else distributionAuthCd = Integer.parseInt(request.getParameter("distributionAuthCd"));


		// userPopupParam 객체 만들어서 저장
		UserPopupParam userPopupParam = UserPopupParam.builder()
				.userId(request.getParameter("userId"))
				.userCd(request.getParameter("userCd"))
//				.userPwd(hashedPassword)
//				.businessAreaCd(request.getParameter("businessArea"))
				.userNm(request.getParameter("userNm"))
				.email(request.getParameter("email"))
				.deptCd(request.getParameter("deptCd"))
				.positionCd(request.getParameter("positionCd"))
				.roleGroupCd(request.getParameter("roleGroupCd"))
//				.authApprovalLevel(distributionAuthCd)
				.authSite(authSite)
				.useYn(useYn)
				.delYn(delYn)
				.protectYn(protectYn)
				.lockYn(lockYn)
				.newUser(newUser)
				.build();

		if ( request.getParameter("saveFlag").equals("I")){ // 생성일때

			if ( request.getParameter("deptCd") == null || request.getParameter("positionCd") == null || request.getParameter("roleGroupCd") == null ){
				resultVo.setMessage("msg.plsSetValue");
				resultVo.setSuccess(false);
				return resultVo;
			}

			if (dao.checkUserId(request.getParameter("userId")) != null){
				resultVo.setMessage("msg.alrExistUser"); // "이미 존재하는 사용자 계정입니다"
				resultVo.setSuccess(false);
				return resultVo;
			} else if (dao.checkUserNm(request.getParameter("userNm")) != null) {
				resultVo.setMessage("msg.alrExistUserName"); // "이미 존재하는 사용자 이름입니다"
				resultVo.setSuccess(false);
				return resultVo;
			} else if(dao.checkEmail(request.getParameter("email")) > 0){
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

		}else if( request.getParameter("saveFlag").equals("U")){ // 수정일때

			// 기존 사용자 정보 가져오기
			UserListVO userPopupParam_old = dao.getUserInfoByUserCd(request.getParameter("userCd"));

			// deptCd , positionCd, roleGroupCd 가 null 값이면 실패
			if ( request.getParameter("deptCd") == null || request.getParameter("positionCd") == null || request.getParameter("roleGroupCd") == null ){
				resultVo.setMessage("msg.plsSetValue");
				resultVo.setSuccess(false);
				return resultVo;
			}

			// 기존 사용자 정보가 null 값이면 실패
			if (userPopupParam_old == null) {
				resultVo.setMessage("msg.userNotFound");
				resultVo.setSuccess(false);
				return resultVo;
			}


			// userId 체크. 변경됐으면, 변경값 db에서 체크. 이미 있으면 실패.
			if (!userPopupParam_old.getUserId().equals(request.getParameter("userId"))) {
				if (dao.checkUserId(request.getParameter("userId")) != null) {
					resultVo.setMessage("msg.alrExistUser");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			// userNm 체크
			if (!userPopupParam_old.getUserNm().equals(request.getParameter("userNm"))) {
				if (dao.checkUserNm(request.getParameter("userNm")) != null) {
					resultVo.setMessage("msg.alrExistUserName");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			// email 체크
			if (userPopupParam_old.getEmail() != null &&
					!userPopupParam_old.getEmail().equals(request.getParameter("email"))) {
				if (dao.checkEmail(request.getParameter("email")) > 0) {
					resultVo.setMessage("msg.alrExistEmail");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}
			dao.editUserInfo(userPopupParam); // 정보 업데이트
			resultVo.setSuccess(true);
			return resultVo;
			} else if( request.getParameter("saveFlag").equals("E")){ // 수정일때. 여기에만 쓰이는 플래그

			// 기존 사용자 정보 가져오기
			UserListVO userPopupParam_old = dao.getUserInfoByUserCd(request.getParameter("userCd"));

			// deptCd , positionCd, roleGroupCd 가 null 값이면 실패
			if ( request.getParameter("deptCd") == null || request.getParameter("positionCd") == null || request.getParameter("roleGroupCd") == null ){
				resultVo.setMessage("msg.plsSetValue");
				resultVo.setSuccess(false);
				return resultVo;
			}

			// 기존 사용자 정보가 null 값이면 실패
			if (userPopupParam_old == null) {
				resultVo.setMessage("msg.userNotFound");
				resultVo.setSuccess(false);
				return resultVo;
			}


			// userId 체크. 변경됐으면, 변경값 db에서 체크. 이미 있으면 실패.
			if (!userPopupParam_old.getUserId().equals(request.getParameter("userId"))) {
				if (dao.checkUserId(request.getParameter("userId")) != null) {
					resultVo.setMessage("msg.alrExistUser");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			// userNm 체크
			if (!userPopupParam_old.getUserNm().equals(request.getParameter("userNm"))) {
				if (dao.checkUserNm(request.getParameter("userNm")) != null) {
					resultVo.setMessage("msg.alrExistUserName");
					resultVo.setSuccess(false);
					return resultVo;
				}
			}

			// email 체크
			if (!userPopupParam_old.getEmail().equals(request.getParameter("email"))) {
				if (dao.checkEmail(request.getParameter("email")) > 0) {
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

		System.out.println(" 실패 ");
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

	}






