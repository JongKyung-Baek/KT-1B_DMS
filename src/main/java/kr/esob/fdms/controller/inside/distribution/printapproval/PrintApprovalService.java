package kr.esob.fdms.controller.inside.distribution.printapproval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.inside.distribution.approval.ApprovalListParam;

@Service
public class PrintApprovalService implements CommonService{

	@Inject
	PrintApprovalDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return 0;
	}
}
