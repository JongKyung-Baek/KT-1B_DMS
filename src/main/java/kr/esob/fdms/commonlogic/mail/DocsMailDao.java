package kr.esob.fdms.commonlogic.mail;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.fdms.controller.inside.cr.CrParam;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.controller.outside.drawing.request.RequestListVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocsMailDao extends AbstractDao{
	private final String prefix = "sql.DocsMail.";

	public MailInfoVO selectReceiveUser(String param) {
		System.out.println(param);System.out.println(param);System.out.println(param);
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

	@SuppressWarnings("unchecked")
	public List<String> selectUnregSecurityUserInfo(Object param){
		return list(prefix + "selectUnregSecurityUserInfo", param);
	}

	public List<MailInfoVO> selectCompanyUserList(Object param){
		return list(prefix + "selectCompanyUserList", param);
	}

	public UserVO selectUserInfo(Object param){
		@SuppressWarnings("unchecked")
		List<UserVO> list = list(prefix + "selectUserInfo", param);

		if(null != list && list.size() > 0) {
			return list.get(0);
		}

		return null;
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
