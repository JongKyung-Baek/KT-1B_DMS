package kr.esob.tdms.controller.main;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class MainDao extends AbstractDao {
	private final String prefix = "sql.Main.";

	public Integer selectSessionTime() {
		return (Integer) obj(prefix + "selectSessionTime");
	}

	public int updateSessionTime(int sessionTime) {
		return update(prefix + "updateSessionTime", sessionTime);
	}
}
