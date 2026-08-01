package kr.esob.tdms.commonlogic.loginhistory;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class HistoryDao extends AbstractDao {
	private String prefix = "sql.loginHistory.";


	public void insertHistory(HistoryParam param) {
		insert(prefix + "insertHistory", param);
	}
}
