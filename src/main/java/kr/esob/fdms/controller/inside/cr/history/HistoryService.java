package kr.esob.fdms.controller.inside.cr.history;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.controller.inside.cr.CommonCrDao;
import kr.esob.fdms.controller.inside.cr.CrInfoVO;
import kr.esob.fdms.controller.inside.cr.CrParam;

@Service
public class HistoryService implements CommonService{

	@Inject
	HistoryDao dao;

	@Inject
	CommonCrDao commonCrDao;

	@SuppressWarnings("rawtypes")
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

	public CrInfoVO selectHistoryInfo(CrParam param) {
		CrInfoVO vo = dao.selectHistoryInfo(param);
		vo.setFileList(commonCrDao.selectInsideFileList(param));
		return vo;
	}

}
