package kr.esob.fdms.controller.inside.production.disposal;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.ApprovalLineDetailVO;

@Service
public class DisposalService implements CommonService{

	@Inject
	DisposalDao dao;

	@Inject
	DocsMailService mailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public ResultVO destroyRequest(DisposalRequestPopupParam param) {
		ResultVO result = new ResultVO();

//		INSERT DOCS_DESTROY_REQUEST
		dao.inserDestoryRequest(param);

		insertRequestInfo(param);
		for(DisposalRequestPopupParam tempParam : param.getList()) {
			tempParam.setList(dao.selectDisposalList(tempParam));

			tempParam.setDestroyRequestNo(param.getDestroyRequestNo());
			tempParam.setRequestDesc(param.getRequestDesc());
			tempParam.setTeamLeaderUid(param.getTeamLeaderUid());
			dao.insertDocsDestoryRequestMapping(tempParam);
			dao.updateApprovalFile(tempParam);
			dao.updateProductStatusDisposal(tempParam);
		}

		result.setSuccess(true);
		if(param.getSendEmailYn().isBooleanValue()) {
			MailInfoVO mailInfoVo = mailService.selectReceiveUser(param.getTeamLeaderUid());
			mailInfoVo.setMailEnum(DocsMailEnum.PRODUCT_DISPOSAL_APPROVAL);
			mailService.sendDocsMail(mailInfoVo);
		}
		return result;
	}


	private void insertRequestInfo(DisposalRequestPopupParam param) {
		List<ApprovalLineDetailVO> appLindDetail = dao.getDocsApprovalLineDetail("3");
		//INSERT DOCS_REQUEST_DETAIL
		for(int i=1; i<=appLindDetail.size(); i++) {
			param.setProcessSeq(Integer.toString(i));
			param.setApprovalStatusCd(appLindDetail.get(i-1).getApprovalStatusCd());
			param.setApprovalGradeCd(appLindDetail.get(i-1).getApprovalGradeCd());
			dao.insertDocsDestroyRequestDetail(param);
		}
	}

}
