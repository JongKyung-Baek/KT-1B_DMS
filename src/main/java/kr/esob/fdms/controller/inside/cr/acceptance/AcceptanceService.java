package kr.esob.fdms.controller.inside.cr.acceptance;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.mail.DocsMailEnum;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.commonlogic.value.CrStatusCdInfo;
import kr.esob.fdms.controller.inside.cr.CommonCrDao;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@Service
public class AcceptanceService implements CommonService{

	@Inject
	CommonCrDao commonCrDao;

	@Inject
	AcceptanceDao dao;

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

//	public void updateList(Object param) {
//		dao.updateList(param);
//	}

	public void deleteList(Object param) {

	}

	public CrInfoVO selectAcceptanceInfo(CrParam param) {
		CrInfoVO vo = dao.selectAcceptanceInfo(param);
		vo.setFileList(commonCrDao.selectInsideFileList(param));
		return vo;
	}

	public ResultVO approvalRequest(CrParam param) {
		log.info("Method {}#approvalRequest is called", this.getClass().getSimpleName());
		ResultVO resultVo = new ResultVO();
		param.setActionCd(Constant.ACCEPT);
		param.setApprovalGradeCd("TL");
		param.setCurrentProcessSeqNo(4);
		param.setStatusCd(CrStatusCdInfo.PURCHASER_ACCEPT);
		param.setApprovalStatusCd(Constant.ACCEPT);
		param.setRequestDesc(param.getReviewResult());
		dao.updateAcceptance(param);
		dao.updateAproval(param);
		dao.updateRequest(param);
		dao.updateCr(param);
		resultVo.setSuccess(true);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectApprovalUserInfo(param);
				mailInfoVo.setMailEnum(DocsMailEnum.CR_APPROVAL);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return resultVo;
	}

	public ResultVO acceptanceReject(CrParam param) {
		ResultVO resultVo = new ResultVO();
		param.setActionCd(Constant.REJECT);
		param.setApprovalStatusCd(Constant.REJECT);
		param.setStatusCd(CrStatusCdInfo.PURCHASER_REJECT);
		param.setRequestDesc(param.getRejectReason());
		dao.updateAcceptance(param);
		dao.updateRequest(param);
		dao.updateCr(param);
		resultVo.setSuccess(true);
		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				MailInfoVO mailInfoVo = mailService.selectCrRequestUserInfo(param);
				mailInfoVo.setMailEnum(DocsMailEnum.CR_STATUS);
				mailService.sendDocsMail(mailInfoVo);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return resultVo;
	}

}
