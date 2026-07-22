package kr.esob.fdms.commonlogic.toolbar;


import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class ToolbarInfoDao extends AbstractDao {

	@SuppressWarnings("unchecked")
	public List<ToolbarInfoVO> selectToolbarInfoList(ToolbarParamVO param) {
		return list("sql.ToolbarInfo.selectList", param);
	}

	/**
	 * 툴바 update
	 * @param param
	 */
	public void updateToolbar(Object param) {
		update("sql.ToolbarInfo.updateToolbar", param);
	}

}
