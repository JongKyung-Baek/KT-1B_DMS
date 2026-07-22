package kr.esob.fdms.controller.inside.distribution.approval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;

@Service
public class ApprovalService implements CommonService{

	@Inject
	ApprovalDao dao;

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

}
