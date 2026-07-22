package kr.esob.fdms.commonlogic.systemconfig;

import java.util.List;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class SystemConfigDao extends AbstractDao {
	private String prefix = "sql.SystemConfig.";

	public List<SystemConfigVO> selectSystemConfig(){
		return list(prefix + "selectSystemConfig");
	}

}
