package kr.esob.fdms.controller.inside.distribution.printdestroyapproval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalPopupListVO;
import kr.esob.fdms.util.DateUtil;

@Service
public class PrintDestroyApprovalService implements CommonService{

	@Inject
	PrintDestroyApprovalDao dao;

	@Inject
	DateUtil dateUtil;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}

	@SuppressWarnings("rawtypes")
	public List selectPopupList(PrintDestroyApprovalListParam param) {
//		param.setList(dao.selectSearchInfo(param));
		return dao.selectPopupList(param);
	}

	@SuppressWarnings("rawtypes")
	public int selectPopupListCount(PrintDestroyApprovalListParam param) {
		param.setList(dao.selectSearchInfo(param));
		return dao.selectPopupListCount(param);
	}

	public ResultVO saveApproval(PrintDestroyApprovalPopupParam param) {
		ResultVO result = new ResultVO();

		// 승인/반려 정보 저장 전 기존 정보 조회
		param = dao.getDestroyRequestInfo(param);
		if( "A".equals(param.getSaveType()) ) {								//승인
			param.setActionCd("APPROVAL");
//			param.setApprovalStatusCd("APPROVAL");
			if( "APPROVAL".equals(param.getApprovalStatusCd()) && "TL".equals(param.getApprovalGradeCd()) && "4".equals(param.getApprovalLineId()) ){	//현재 구매팀장 결재면서 결재 라인이 1번이면 방산팀장 결재로 보내기
				param.setCurrentProcessSeqNo(String.valueOf(Integer.parseInt(param.getCurrentProcessSeqNo()) + 1));
				param.setStatusCd("REQUEST");
				dao.updatePrintDestroyRequestInfo(param);
			}else {															// 최종승인
				param.setStatusCd("APPROVAL");
				dao.updatePrintDestroyRequestInfo(param);
				//최종승인인 경우 요청의 requestNo + objectId의 아이템 미사용으로 변경
				List<PrintDestroyItemListVO> itemList = dao.selectDestroyItemList(param);
				for(PrintDestroyItemListVO tempVo : itemList) {
					PrintDestroyApprovalPopupParam tempParam = new PrintDestroyApprovalPopupParam();
					tempParam.setObjectId(tempVo.getObjectId());
					tempParam.setRequestNo(tempVo.getRequestNo());
					dao.updateRequestMapping(tempParam);
				}
			}
			param.setRejectDesc(null);

		}else if( "R".equals(param.getSaveType()) ) {						//반려
			param.setActionCd("REJECT");
			param.setStatusCd("REJECT");
			dao.updatePrintDestroyRequestInfo(param);
		}

		dao.updatePrintDestroyRequestDetail(param);

		result.setSuccess(true);
		return result;
	}

	public PrintDestroyItemListVO getDestroyRequest(String destroyRequestNo) {
		return dao.getDestroyRequest(destroyRequestNo);
	}

}
