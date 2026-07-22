package kr.esob.fdms.controller.inside.production.history;

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

	public HistoryDetailVO selectHistoryDetailInfo(HistoryDetailParam param) {
		HistoryDetailVO vo = dao.selectHistoryDetail(param);
		vo.setProductionInfoVoList(dao.selectProductionList(param));
		vo.setRequestDetailVoList(dao.selectRequestDetailList(param));
		vo.setRequestDeployVoList(selectRequestDeployList(param));
		return vo;
	}

	public List<RequestDeployVO> selectRequestDeployList(HistoryDetailParam param){
		List<RequestDeployVO> deployList = dao.selectRequestDeployList(param);
		for(RequestDeployVO vo : deployList) {
			vo.setDeployInfoList(dao.selectDeployInfoList(vo));
		}
		return deployList;
	}
}
