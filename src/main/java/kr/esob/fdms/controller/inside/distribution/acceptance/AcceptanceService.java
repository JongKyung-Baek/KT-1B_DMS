package kr.esob.fdms.controller.inside.distribution.acceptance;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.combo.ComboInfoVO;
import kr.esob.fdms.commonlogic.mail.DocsMailService;
import kr.esob.fdms.commonlogic.mail.MailInfoVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.login.UserVO;
import kr.esob.fdms.util.DateUtil;

@Service
public class AcceptanceService implements CommonService{

	@Inject
	AcceptanceDao dao;

	@Inject
	DateUtil dateUtil;

	@Inject
	DocsMailService mailService;

	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	public List<ComboInfoVO> getDefenseTeamLeader() {
		return dao.getDefenseTeamLeader();
	}

	public AcceptanceVO getDocRequestData(String requestNo) {
		return dao.getDocRequestData(requestNo);
	}


	@SuppressWarnings("static-access")
	public ResultVO saveAcceptance(AcceptanceParam param) {
		ResultVO result = new ResultVO();

		if( "A".equals(param.getSaveType()) ) {			//접수
			param.setActionCd("ACCEPT");
			param.setCurrentProcessSeqNo("3");
			dao.updateTlRequestDetail(param);			//결재자
			if("1".equals(param.getApprovalLineId())) {	//방산팀장 결재 여부에 따른 4단계 결재 정보 수정
				param.setActionCd("ACCEPT");
				dao.saveDefRequestDetail(param);
			}
			//배포 방식
			/*
			if("general".equals(param.getFileDistributionType())) {
				param.setDeployNormalYn("Y");
				param.setDeploySpecialYn("N");
			}else if("security".equals(param.getFileDistributionType())) {
				param.setDeployNormalYn("N");
				param.setDeploySpecialYn("Y");
			}else {
				param.setDeployNormalYn("N");
				param.setDeploySpecialYn("N");
			}
			*/

			List<AcceptanceParam> list = param.getList();

			for(AcceptanceParam file : list) {
				file.setUseStartYmd(dateUtil.getToday("yyyyMMdd"));								//배포 기한
				file.setUseEndYmd(dateUtil.getAddMonth(file.getDeployTerm(), "yyyyMMdd"));		//유효기간(개월)

				dao.updateRequestFile(file);
			}
		}else if( "R".equals(param.getSaveType()) ) {	//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
		}

		try {
			if(param.getSendEmailYn().isBooleanValue()) {
				mailService.sendDocsMail(mailService.selectReceiveUser(param.getPurchaseUid()));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}


		dao.saveAccept(param);
		result.setSuccess(true);
		return result;
	}

}
