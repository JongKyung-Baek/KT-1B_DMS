package kr.esob.tdms.controller.general.distribution.printapproval;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.controller.general.distribution.approval.ApprovalListParam;

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
