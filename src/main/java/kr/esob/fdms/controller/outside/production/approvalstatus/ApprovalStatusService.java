package kr.esob.fdms.controller.outside.production.approvalstatus;

import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestDao;

@Service
public class ApprovalStatusService implements CommonService {

	@Inject
	ApprovalStatusDao dao;

	@Inject
	CommonRequestDao requestDao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}
}