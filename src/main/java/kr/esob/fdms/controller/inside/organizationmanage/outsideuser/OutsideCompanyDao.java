package kr.esob.fdms.controller.inside.organizationmanage.outsideuser;


import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.user.information.InformationListParam;

@Repository
public class OutsideCompanyDao extends AbstractDao {
	private String prefix = "sql.OrganizationmanageOutsideCompany.";

	@SuppressWarnings("rawtypes")
	public List selectList(Object param) {
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param) {
		return (Integer) obj(prefix + "selectListCount", param);
	}
	
	public Integer selectCompanyCheck(CompanyPopupParam param){
		return (Integer) obj(prefix + "selectCompanyCheck", param);
	}

	public CompanyInfoVO selectCompanyInfo(Object param) {
		return (CompanyInfoVO) obj(prefix + "selectCompanyInfo", param);
	}

	public void insertCompany(CompanyPopupParam param) {
		insert(prefix + "insertCompany", param);
	}

	public void updateCompany(CompanyPopupParam param) {
		insert(prefix + "updateCompany", param);
	}

	public String selectCompanyCd(Object param) {
		return (String) obj(prefix + "selectCompanyCd", param);
	}

	public Integer selectItnVendorSerial(Object param) {
		return (Integer) obj(prefix + "selectItnVendorSerial", param);
	}

	public void insertItnVendor(Object param) {
		insert(prefix + "insertItnVendor", param);
	}

	public void updateItnVendor(Object param) {
		update(prefix + "updateItnVendor", param);
	}


	/**
	 * BUSINESS_AREA_CD, 사업자 코드로 SELECT
	 * @param param
	 * @return
	 */
	public Integer selectItnVendorCount(Object param) {
		return (Integer) obj(prefix + "selectItnVendorCount", param);
	}

	public void deleteCompanyPurchaser(Object param) {
		update(prefix + "deleteCompanyPurchaser", param);
	}

	public void insertCompanyPurchaser(Object param) {
		insert(prefix + "insertCompanyPurchaser", param);
	}
}
