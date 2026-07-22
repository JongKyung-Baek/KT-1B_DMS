package kr.esob.fdms.controller.inside.distribution.oldhistory;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import kr.esob.fdms.commonlogic.abstractclass.CommonService;
import kr.esob.fdms.commonlogic.result.ResultVO;

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
	
	public ResultVO destroyOldHistory(HistoryListParam param) {
		ResultVO result = new ResultVO();
		
		try {
			for(HistoryListParam row : param.getList()) {
				dao.destroyOldHistory(row);
			}
			
			result.setSuccess(true);
		}
		catch(Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			result.setFailReason(e.getMessage());
		}
		
		return result;
	}
}
