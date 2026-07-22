package kr.esob.fdms.controller.inside.distribution.oldcr;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;

@Service
public class HistoryService implements CommonService{

	@Inject
	HistoryDao dao;

	@SuppressWarnings("rawtypes")
	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object obj) {
		return dao.selectListCount(obj);
	}
}
