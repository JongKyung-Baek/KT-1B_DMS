package kr.esob.fdms.commonlogic.grid;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

@Service
public class GridInfoService {

	@Inject
	GridInfoDao dao;
	
	public List<GridInfoVO> selectGridInfo(String gridId) {
		return dao.selectGridInfoList(gridId);
	}
}
