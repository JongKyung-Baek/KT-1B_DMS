package kr.esob.fdms.commonlogic.loginhistory;

import kr.esob.fdms.commonlogic.seq.SeqDao;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Service
public class HistoryService {

	@Inject
	HistoryDao dao;
	@Inject
	SeqDao seqDao;

	public void insertHistory(String loginType, HttpServletRequest request) {
		HashMap<String, Object> map = new HashMap<>();
		HistoryParam param = new HistoryParam();
		map.put("tableNm", "DOCS_LOGIN_HISTORY");
		param.setHistorySeq(seqDao.getSeq(map));
		param.setAccessIp(request.getRemoteAddr());
		param.setLoginType(loginType);

		dao.insertHistory(param);
		map.remove("tableNm");
	}
}
