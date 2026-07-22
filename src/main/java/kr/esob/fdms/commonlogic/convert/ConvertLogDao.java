package kr.esob.fdms.commonlogic.convert;

import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;


@Repository
public class ConvertLogDao extends AbstractDao {
	private String prefix = "sql.convert.";


	public void insertConvertLog(Map<String, Object> param) {
		insert(prefix + "insertConvertLog", param);
	}
}