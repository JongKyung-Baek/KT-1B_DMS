package kr.esob.tdms.commonlogic.viewer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ViewerTicketDao extends AbstractDao {
    private static final String PREFIX = "sql.ViewerTicket.";

    public int deleteExpired() {
        return delete(PREFIX + "deleteExpired", null);
    }

    public int insertTicket(ViewerTicketVO ticket) {
        return (Integer) insert(PREFIX + "insertTicket", ticket);
    }

    public ViewerTicketVO selectValid(String key, String userCd, String sessionId) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("key", key);
        param.put("userCd", userCd);
        param.put("sessionId", sessionId);
        return (ViewerTicketVO) objNotUseSession(PREFIX + "selectValid", param);
    }

    public int markUsed(String key, String userCd, String sessionId) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("key", key);
        param.put("userCd", userCd);
        param.put("sessionId", sessionId);
        return update(PREFIX + "markUsed", param);
    }
}
