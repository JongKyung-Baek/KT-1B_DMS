package kr.esob.fdms.controller.inside.outregisted.accept;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.outside.commonrequest.CommonRequestService;

@Service
public class AcceptService implements CommonService {

	@Inject
	AcceptDao dao;

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
}
