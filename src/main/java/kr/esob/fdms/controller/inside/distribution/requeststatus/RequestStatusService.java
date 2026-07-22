package kr.esob.fdms.controller.inside.distribution.requeststatus;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.distribution.commonrequest.CommonApprovalParam;
import kr.esob.fdms.controller.inside.distribution.history.HistoryListVO;
import kr.esob.fdms.util.DateUtil;

@Service
public class RequestStatusService implements CommonService{

	@Inject
	RequestStatusDao dao;

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

	public int selectPopupListCount(RequestStatusPopupParam param) {
		return dao.selectPopupListCount(param);
	}

	public RequestStatusPopupVO getApprovalRequestStatus(RequestStatusPopupParam param) {
		return dao.getApprovalRequestStatus(param);
	}

	public List requestStatusPopupList(RequestStatusPopupParam param) {
		return dao.requestStatusPopupList(param);
	}
	
	public ResultVO deleteRequestNo(RequestStatusPopupParam param) {
		ResultVO resultVo = new ResultVO();
		dao.deleteRequest(param);
		dao.deleteRequestMapping(param);
		dao.deleteRequestDetail(param);
		dao.deleteRequestFile(param);
		dao.deleteRequestDeploy(param);
		resultVo.setSuccess(true);
		return resultVo;
	}
}
