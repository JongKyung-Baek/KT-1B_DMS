package kr.esob.fdms.controller.inside.unregisted.approval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;

@Service
public class ApprovalService implements CommonService {

	@Inject
	ApprovalDao dao;

	@Inject
	CommonRequestService commonRequestService;

	@Inject
	DocsMailService mailService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public ApprovalPopupVO getRequestInfo(ApprovalPopupParam param) {
		return dao.getRequestInfo(param);
	}

	public List<GridResultVO> selectPopupList(ApprovalPopupParam param) {
		return dao.selectPopupList(param);
	}

	public ResultVO saveApproval(ApprovalPopupParam param) {
		ResultVO result = new ResultVO();
		if( "A".equals(param.getSaveType()) ) {			//승인
			param.setActionCd("APPROVAL");
			param.setApprovalStatusCd("APPROVAL");
			param.setStatusCd("APPROVAL");
			//승인으로 변경
			dao.updateRequestInfo(param);
			// 파일 결재 정보 테이블(DOCS_APPROVAL_FILE)에 각 아이템 추가
			List<ApprovalPopupVO> itemList = dao.selectItemList(param);
			for(ApprovalPopupVO tempVo : itemList) {
				ApprovalPopupParam tempParam = new ApprovalPopupParam();
				tempParam.setObjectId(tempVo.getObjectId());
				tempParam.setRequestNo(param.getRequestNo());
				tempParam.setFileNo(tempVo.getFileNo());
				dao.insertApprovalFile(tempParam);
			}
			param.setRejectDesc(null);
			try {
				if(param.getSendEmailYn().isBooleanValue()) {
					MailInfoVO mailInfoVo = mailService.selectDeployUserInfo(param);
					mailInfoVo.setToCc(mailService.selectUnregSecurityUserInfo(param));
					mailInfoVo.setMailEnum(DocsMailEnum.UNREG_STATUS);
					mailInfoVo.setFromMail(mailService.selectPurchaserEmail(param));
					mailService.sendDocsMail(mailInfoVo);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			param.setApprovalStatusCd("REJECT");
			dao.updateRequestInfo(param);
		}
		dao.updateRequestDetail(param);
		result.setSuccess(true);

		return result;
	}

	public int selectPopupListCount(ApprovalPopupParam param) {
		return dao.selectPopupListCount(param);
	}

}
