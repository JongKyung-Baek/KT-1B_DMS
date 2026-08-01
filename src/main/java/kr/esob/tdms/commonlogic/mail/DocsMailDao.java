package kr.esob.tdms.commonlogic.mail;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.controller.general.cr.CrParam;
import kr.esob.tdms.controller.general.distribution.commonrequest.CommonApprovalParam;
import kr.esob.tdms.commonlogic.distribution.model.RequestListVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocsMailDao extends AbstractDao{
	private final String prefix = "sql.DocsMail.";

	public MailInfoVO selectReceiveUser(String param) {
		return (MailInfoVO) obj(prefix + "selectReceiveUser", param);
	}

	public MailInfoVO selectApprovalUserInfo(CrParam param) {
		return (MailInfoVO) obj(prefix + "selectApprovalUserInfo", param);
	}

	public MailInfoVO selectDeployUserInfo(Object param) {
		return (MailInfoVO) obj(prefix + "selectDeployUserInfo", param);
	}

	public MailInfoVO selectDefUserInfo(CommonApprovalParam param) {
		return (MailInfoVO) obj(prefix + "selectDefUserInfo", param);
	}

	public MailInfoVO selectRequestUserInfo(Object param) {
		return (MailInfoVO) obj(prefix + "selectRequestUserInfo", param);
	}

	public MailInfoVO selectCrRequestUserInfo(CrParam param) {
		return (MailInfoVO) obj(prefix + "selectCrRequestUserInfo", param);
	}

	public List<MailInfoVO> selectCompanyUserList(Object param){
		return list(prefix + "selectCompanyUserList", param);
	}

	public String selectPurchaserEmail(Object param) {
		return (String) obj(prefix + "selectPurchaserEmail", param);
	}

	public int insertMail(Object param){
		return (Integer)insert(prefix + "insertMail", param);
	}

	@SuppressWarnings("unchecked")
	public List<MailInfoVO> selectMail(){
		return list(prefix + "selectMail");
	}

	public void updateMail(Object param){
		insert(prefix + "updateMail", param);
	}

	public List<RequestListVO> selectRevisionData(Object param){
		return list(prefix + "selectRevisionCoreData", param);
	}

}
