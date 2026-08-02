package kr.esob.tdms.commonlogic.viewerintegration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;

@Repository
public class ViewerIntegrationDao extends AbstractDao {
    private static final String PREFIX = "sql.ViewerIntegration.";

    public int insertLaunchWithHistory(ViewerLaunchRecord launch) {
        return (Integer) insert(PREFIX + "insertLaunchWithHistory", launch);
    }

    public ViewerLaunchRecord selectLaunch(String correlationId) {
        return (ViewerLaunchRecord) objNotUseSession(PREFIX + "selectLaunch", correlationId);
    }

    public int deleteExpiredState(int retentionDays) {
        return delete(PREFIX + "deleteExpiredState", retentionDays);
    }

    public int deleteOldNonces() {
        return delete(PREFIX + "deleteOldNonces", null);
    }

    public int insertNonce(String clientId, String nonce) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("clientId", clientId);
        values.put("nonce", nonce);
        return (Integer) insert(PREFIX + "insertNonce", values);
    }

    public int insertEvent(ViewerCallbackEvent event) {
        return (Integer) insert(PREFIX + "insertEvent", event);
    }

    public int markViewed(ViewerCallbackEvent event) {
        return update(PREFIX + "markViewed", event);
    }
}
