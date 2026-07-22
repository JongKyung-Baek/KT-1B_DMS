package kr.esob.fdms.controller.inside.pdm.deployhistory;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;

@Service
public class PdmDeployHistoryService implements CommonService{

	@Inject
	PdmDeployHistoryDao dao;

	@Override
	public List selectList(Object param) {
		return dao.selectList(param);
	}

	@Override
	public int selectListCount(Object param) {
		return dao.selectListCount(param);
	}

	public void updateList(Object param) {
		dao.updateList(param);
	}

	public void deleteList(Object param) {

	}


}
