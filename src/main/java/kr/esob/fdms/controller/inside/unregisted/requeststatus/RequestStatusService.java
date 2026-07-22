package kr.esob.fdms.controller.inside.unregisted.requeststatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;

@Service
public class RequestStatusService implements CommonService {

	@Inject
	RequestStatusDao dao;

	@Inject
	CommonRequestService commonRequestService;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public RequestStatusPopupVO getRequestInfo(RequestStatusPopupParam param) {
		return dao.getRequestInfo(param);
	}

	public List<GridResultVO> selectPopupList(RequestStatusListParam param) {
		return dao.selectPopupList(param);
	}

	public List<String> getDeployEmailCC(Object param) {
		String deployEmailCC = dao.getDeployEmailCC(param);
		List<String> rtnList = new ArrayList<String>();
		if( !(null==deployEmailCC) &&(!"".equals(deployEmailCC))) {
			rtnList = Arrays.asList(deployEmailCC.split(","));
		}
		return rtnList;
	}
	
	public ResultVO deleteRequestNo(RequestStatusPopupParam param) {
		ResultVO resultVo = new ResultVO();
		dao.deleteRequest(param);
		dao.deleteRequestMapping(param);
		dao.deleteRequestDetail(param);
		dao.deleteRequestFile(param);
		dao.deleteRequestDeploy(param);
		dao.deleteApprovalFile(param);
		resultVo.setSuccess(true);
		return resultVo;
	}
	
}
