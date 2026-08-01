package kr.esob.tdms.controller.general.cr.history;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.tdms.commonlogic.abstractclass.CommonService;
import kr.esob.tdms.controller.general.cr.CommonCrDao;
import kr.esob.tdms.controller.general.cr.CrInfoVO;
import kr.esob.tdms.controller.general.cr.CrParam;

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
