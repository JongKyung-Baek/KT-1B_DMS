package kr.esob.tdms.controller.general.distribution.accountrequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.controller.login.UserVO;

@Repository
public class DistributionAccountRequestDao extends AbstractDao {
    private static final String PREFIX = "sql.DistributionAccountRequest.";

    public int deleteExpiredNonces(int retentionDays) {
        return delete(PREFIX + "deleteExpiredNonces", Integer.valueOf(retentionDays));
    }

    public int insertNonce(String clientId, String nonce) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("clientId", clientId);
        values.put("nonce", nonce);
        return (Integer) insert(PREFIX + "insertNonce", values);
    }

    public Long insertRequest(DistributionAccountRequestRecord request) {
        Number result = (Number) objNotUseSession(PREFIX + "insertRequest", request);
        return result == null ? null : Long.valueOf(result.longValue());
    }

    public DistributionAccountRequestRecord selectBySourceEvent(
            String sourceSystemId, String eventId) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("sourceSystemId", sourceSystemId);
        values.put("eventId", eventId);
        return (DistributionAccountRequestRecord) objNotUseSession(
            PREFIX + "selectBySourceEvent", values);
    }

    public DistributionAccountRequestRecord selectBySourceCorrelation(
            String sourceSystemId, String correlationId) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("sourceSystemId", sourceSystemId);
        values.put("correlationId", correlationId);
        return (DistributionAccountRequestRecord) objNotUseSession(
            PREFIX + "selectBySourceCorrelation", values);
    }

    public DistributionAccountRequestRecord selectExternalStatus(
            String clientId, String sourceSystemId, String eventId) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("clientId", clientId);
        values.put("sourceSystemId", sourceSystemId);
        values.put("eventId", eventId);
        return (DistributionAccountRequestRecord) objNotUseSession(
            PREFIX + "selectExternalStatus", values);
    }

    public DistributionAccountRequestRecord selectRequest(long requestId) {
        return (DistributionAccountRequestRecord) objNotUseSession(
            PREFIX + "selectRequest", Long.valueOf(requestId));
    }

    public DistributionAccountRequestRecord selectRequestForUpdate(long requestId) {
        return (DistributionAccountRequestRecord) objNotUseSession(
            PREFIX + "selectRequestForUpdate", Long.valueOf(requestId));
    }

    @SuppressWarnings("unchecked")
    public List<DistributionAccountRequestRecord> selectRequests(
            String status, String requestType, String sourceSystemId,
            String keyword, int limit, int offset) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("status", status);
        values.put("requestType", requestType);
        values.put("sourceSystemId", sourceSystemId);
        values.put("keyword", keyword);
        values.put("limit", Integer.valueOf(limit));
        values.put("offset", Integer.valueOf(offset));
        return listNotUseSession(PREFIX + "selectRequests", values);
    }

    @SuppressWarnings("unchecked")
    public List<DistributionAccountRequestEvent> selectEvents(long requestId) {
        return listNotUseSession(PREFIX + "selectEvents", Long.valueOf(requestId));
    }

    public int insertReceivedEvent(DistributionAccountRequestRecord request) {
        return (Integer) insert(PREFIX + "insertReceivedEvent", request);
    }

    public int insertDecisionEvent(long requestId, String eventType,
            String fromStatus, String toStatus, String comment, UserVO actor) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("requestId", Long.valueOf(requestId));
        values.put("eventType", eventType);
        values.put("fromStatus", fromStatus);
        values.put("toStatus", toStatus);
        values.put("comment", comment);
        values.put("actor", actor);
        return (Integer) insert(PREFIX + "insertDecisionEvent", values);
    }

    public int decide(long requestId, String expectedStatus, String targetStatus,
            String comment, UserVO actor) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("requestId", Long.valueOf(requestId));
        values.put("expectedStatus", expectedStatus);
        values.put("targetStatus", targetStatus);
        values.put("comment", comment);
        values.put("actor", actor);
        return update(PREFIX + "decide", values);
    }
}
