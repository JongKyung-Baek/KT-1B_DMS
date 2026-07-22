package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.user.information.InformationListParam;
import kr.esob.fdms.util.ObjectUtil;
import kr.esob.fdms.util.StringUtil;

@Service
public class OutsideCompanyService implements CommonService {

	@Inject
	OutsideCompanyDao dao;

	@Inject
	OutsideuserDao userDao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object obj) {
		return dao.selectList(obj);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public CompanyInfoVO selectCompanyInfo(Object param) {
		return dao.selectCompanyInfo(param);
	}
	
	public ResultVO selectCompanyCheck(CompanyPopupParam param)  throws Exception {
		ResultVO resultVo = new ResultVO();

		if(param.getSaveFlag().equals("I")) {
		if (dao.selectCompanyCheck(param) == 0)
			resultVo.setSuccess(true);
		else
			resultVo.setSuccess(false);
		}else {
			resultVo.setSuccess(true);
		}

		return resultVo;
	}
	
	public ResultVO saveCompany(CompanyPopupParam param) {
		ResultVO result = new ResultVO();
		result.setSuccess(false);

		if(param.getSaveFlag().equals("I")) {
			param.setCompanyCd(dao.selectCompanyCd(param));
			dao.insertCompany(param);
		}
		else {
			dao.updateCompany(param);
		}

		saveCompanyPurchaser(param);
		saveItnVendor(param);
		result.setSuccess(true);

		return result;
	}

	private void saveCompanyPurchaser(CompanyPopupParam param) {

		dao.deleteCompanyPurchaser(param);

		if(!String.valueOf(param.getDistPurchaserUserCd1()).isEmpty() || !String.valueOf(param.getDistPurchaserUserCd1()).isEmpty()) {
			param.setBusinessAreaCd(Constant.BUSINESS_AREA_CD_1);
			param.setDistPurchaserUserCd(param.getDistPurchaserUserCd1());
			param.setCrPurchaserUserCd(param.getCrPurchaserUserCd1());

			dao.insertCompanyPurchaser(param);
		}

		if(!String.valueOf(param.getDistPurchaserUserCd2()).isEmpty() || !String.valueOf(param.getDistPurchaserUserCd2()).isEmpty()) {
			param.setBusinessAreaCd(Constant.BUSINESS_AREA_CD_2);
			param.setDistPurchaserUserCd(param.getDistPurchaserUserCd2());
			param.setCrPurchaserUserCd(param.getCrPurchaserUserCd2());

			dao.insertCompanyPurchaser(param);
		}
	}

	/**
	 * ITN_VENDOR 테이블에 데이터 저장
	 * @param param
	 */
	private void saveItnVendor(CompanyPopupParam param) {
		// 1사업장, 2사업장 구매담당자가 둘다 지정이 되었다면 둘다 insert/update 해야 함.
		if(null != param.getDistPurchaserUserCd1() || null != param.getDistPurchaserUserCd1()) {
			param.setCnSerial(dao.selectItnVendorSerial(param));
			param.setBusinessAreaCd(Constant.BUSINESS_AREA_CD_1);

			if(!String.valueOf(param.getDistPurchaserUserCd1()).isEmpty()) {
				UserListVO user = userDao.selectUser(param.getDistPurchaserUserCd1());
				param.setDistPurchaserUid(user.getUserId());
				param.setDistPurchaserUidEmail(user.getEmail());
			}

//			if(!String.valueOf(param.getCrPurchaserUserCd1()).isEmpty()) {
//				UserListVO user = userDao.selectUser(param.getCrPurchaserUserCd1());
//				param.setCrPurchaserUid(user.getUserId());
//				param.setCrPurchaserUidEmail(user.getEmail());
//			}

			if(dao.selectItnVendorCount(param) > 0) {
				dao.updateItnVendor(param);
			}
			else {
				dao.insertItnVendor(param);
			}

		}

//		if(!String.valueOf(param.getDistPurchaserUserCd2()).isEmpty() || !String.valueOf(param.getDistPurchaserUserCd2()).isEmpty()) {
//			param.setCnSerial(dao.selectItnVendorSerial(param));
//			param.setBusinessAreaCd(Constant.BUSINESS_AREA_CD_2);
//
//			if(!String.valueOf(param.getDistPurchaserUserCd2()).isEmpty()) {
//				UserListVO user = userDao.selectUser(param.getDistPurchaserUserCd2());
//				param.setDistPurchaserUid(user.getUserId());
//				param.setDistPurchaserUidEmail(user.getEmail());
//			}
//
//			if(!String.valueOf(param.getDistPurchaserUserCd2()).isEmpty()) {
//				UserListVO user = userDao.selectUser(param.getCrPurchaserUserCd2());
//				param.setCrPurchaserUid(user.getUserId());
//				param.setCrPurchaserUidEmail(user.getEmail());
//			}
//
//			if(dao.selectItnVendorCount(param) > 0) {
//				dao.updateItnVendor(param);
//			}
//			else {
//				dao.insertItnVendor(param);
//			}
//		}
	}
}
