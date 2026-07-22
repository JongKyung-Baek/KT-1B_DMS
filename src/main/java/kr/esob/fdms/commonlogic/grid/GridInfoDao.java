package kr.esob.fdms.commonlogic.grid;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class GridInfoDao extends AbstractDao {
	@SuppressWarnings("unchecked")
	public List<GridInfoVO> selectGridInfoList(String gridId){
		GridParamVO vo = new GridParamVO();
		vo.setGridId(gridId);

		return list("sql.GridInfo.selectList", vo);
	}

}
