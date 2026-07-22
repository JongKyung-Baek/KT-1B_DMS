package kr.esob.fdms.commonlogic.seq;

import kr.esob.fdms.commonlogic.abstractclass.AbstractDao;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class SeqDao extends AbstractDao {
	private String prefix = "sql.seq.";


	public Long getSeq(HashMap<String, Object> param) {

		Long seq = null;
		if (param.containsKey("sessionYn")) {
			if (param.get("sessionYn").equals("Y")) {
				seq = (Long) obj(prefix + "getSeq", param);
			} else if (param.get("sessionYn").equals("N")) {
				seq = (Long) objNotUseSession(prefix + "getSeq", param);
			}
		} else{
			seq = (Long) obj(prefix + "getSeq", param);
		}

		if(null == seq) {
			seq = 1L;
			param.put("seq", seq);
			update(prefix + "insertSeq", param);
		}
		else {
			seq += 1;
			param.put("seq", seq);
			update(prefix + "updateSeq", param);
		}

		return seq;
	}
}
