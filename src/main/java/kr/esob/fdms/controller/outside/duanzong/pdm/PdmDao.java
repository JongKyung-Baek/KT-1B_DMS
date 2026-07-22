package kr.esob.fdms.controller.outside.duanzong.pdm;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class PdmDao extends AbstractDao {
	private String prefix = "sql.DuanzongPdm.";

	@SuppressWarnings("unchecked")
	public List<PdmListVO> selectList(Object param){
		return list(prefix + "selectList", param);
	}

	public Integer selectListCount(Object param){
		return (Integer) obj(prefix + "selectListCount", param);
	}
}