package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.doc_pdf_link_request.DocPdfLinkRequestDao;
import kr.esob.fdms.util.seed.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Service
public class OutsideuserService implements CommonService {

	@Inject
	OutsideuserDao dao;

	@Autowired
	DocPdfLinkRequestDao dao_for_pwd;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object obj) {
		return dao.selectList(obj);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public void insertUser(CompanyPopupParam param) {
		dao.insertUser(param);
	}


	public void updateUser(CompanyPopupParam param) {
		dao.updateUser(param);
	}

	public UserListVO selectUser(String userCd) {
		return dao.selectUser(userCd);
	}

	public ResultVO saveUser(UserPopupParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		if(param.getSaveFlag().equals("I")) {
			List<Map<String,Object>> dbConfig = dao_for_pwd.selectDbConfig();
			String basicPassword="";

			for(Map<String,Object> config : dbConfig) {
				if(config.get("SYSTEM_CONFIG_CD").equals("BASIC_PASSWORD")) {
					basicPassword = config.get("SYSTEM_CONFIG_VALUE").toString();
				}
			}
			// 초기 비밀번호인 0000 삽입
			param.setUserPwd(PasswordUtils.hashPasswordWithSalt(basicPassword));
			param.setUserCd(dao.selectUserCd(param));
			dao.insertUser(param);
		}
		else {
			dao.updateUser(param);
		}

		result.setSuccess(true);

		return result;
	}
	
	public ResultVO saveAppUserCd(UserPopupParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		dao.updateAppUserCd(param);

		result.setSuccess(true);

		return result;
	}

	/**
	 * 계정잠금 해제
	 * @param param
	 * @return
	 */
	public ResultVO updateUnlockAccount(UserPopupParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		String userCd = param.getUserCd();
		String[] arr = userCd.split(",");

		for(int i=0; i<arr.length; i++) {
			param.setUserCd(arr[i]);
			dao.updateUnlockAccount(param);
		}

		result.setSuccess(true);

		return result;
	}
}
