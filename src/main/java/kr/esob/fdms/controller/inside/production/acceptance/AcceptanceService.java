package kr.esob.fdms.controller.inside.production.acceptance;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.grid.GridResultVO;
import kr.esob.fdms.commonlogic.result.ResultVO;
import kr.esob.fdms.controller.inside.production.approval.ApprovalService;
import kr.esob.fdms.util.DateUtil;

@Service
public class AcceptanceService implements CommonService{

	@Inject
	AcceptanceDao dao;

	@Inject
	DateUtil dateUtil;
	
	@Inject
	ApprovalService service;

	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public List<AcceptancePopupVO> selectPopupList(AcceptancePopupParam param) {
		return dao.selectPopupList(param);
	}

	public int selectPopupListCount(AcceptancePopupParam param) {
		return dao.selectPopupListCount(param);
	}

	public ResultVO saveAcceptance(AcceptancePopupParam param) {
		ResultVO result = new ResultVO();
		dao.updateAcceptance(param);
		service.updateProductStatus(param);
		result.setSuccess(true);
		return result;
	}

}
