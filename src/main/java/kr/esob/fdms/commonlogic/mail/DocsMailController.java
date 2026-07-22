package kr.esob.fdms.commonlogic.mail;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.esob.fdms.commonlogic.abstractclass.AbstractController;
import kr.esob.fdms.commonlogic.result.ResultVO;

@Controller
@RequestMapping("/inside/mail/fdmsMail")
public class DocsMailController extends AbstractController{

	@Inject
	DocsMailService service;

//	@RequestMapping("/sendDistributionRequestMail")
//	public ResultVO sendDistributionRequestMail(MailInfoVO param) {
//		return service.sendRequestMail(param);
//	}
//
//	@RequestMapping("/sendMail")
//	public ResultVO sendMail(MailInfoVO param) {
//		ResultVO resultVo = new ResultVO();
//		String requestType = param.getRequestType();
//		String status = param.getStatus();
//		switch(requestType) {
//		case "DISTRIBUTION":
//			switch(status) {
//			case "REQUEST":
//				resultVo = service.sendRequestMail(param);
//				break;
//			case "APPROVAL":
//				resultVo = service.sendApprovalMail(param);
//				break;
//			}
//			break;
//		case "PRINT":
//			resultVo = service.sendPrintRequestMail(param);
//			break;
//		case "CR":
//			switch(status) {
//			case "REQUEST":
//				resultVo = service.sendCrRequestMail(param);
//				break;
//			case "REJECT":
//				resultVo = service.sendCrRejectMail(param);
//			}
//			break;
//		case "PRODUCT":
//			switch(status) {
//			case "REQUEST":
//				resultVo = service.sendProductRequestMail(param);
//				break;
//			case "APPROVAL":
//				resultVo = service.sendProductApprovalMail(param);
//				break;
//			}
//			break;
//		case "UNREG":
//			switch(status) {
//			case "REQUEST":
//				resultVo = service.sendUnregRequestMail(param);
//				break;
//			case "APPROVAL":
//				resultVo = service.sendUnregApprovalMail(param);
//				break;
//			}
//			break;
//		case "USER":
//			resultVo = service.sendUserRequestMail(param);
//			break;
//		case "DESTROY":
//			switch(status) {
//			case "REQUEST":
//				resultVo = service.sendDestroyRequestMail(param);
//				break;
//			}
//			break;
//
//		}
//		return resultVo;
//	}

}
