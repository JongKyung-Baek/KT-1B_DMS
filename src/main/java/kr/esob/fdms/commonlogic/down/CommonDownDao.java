package kr.esob.fdms.commonlogic.down;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@SuppressWarnings("unchecked")
@Repository
public class CommonDownDao extends AbstractDao {
	private String prefix = "sql.CommonDown.";

	public Map<String, Object> getUploadConfig() {
		return (Map<String, Object>) get(prefix + "getUpdownConfig");
	}

	public List selectList(Object param) {
		return list(prefix + "selectList", param);
	}
}
