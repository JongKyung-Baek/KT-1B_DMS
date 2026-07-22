package kr.esob.fdms.commonlogic.GarbageCleaner;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@SuppressWarnings("unchecked")
@Repository
public class GarbageCleanerDao extends AbstractDao {
	private String prefix = "sql.garbageCleaner.";
	
	//Select DB Config
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> selectDbConfig() {
		return listNotUseSession(prefix + "selectConfig");
	}
	
	

}
