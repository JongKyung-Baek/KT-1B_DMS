package kr.esob.tdms.controller.general.distribution.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import kr.esob.tdms.commonlogic.abstractclass.AbstractDao;
import kr.esob.tdms.controller.login.UserVO;

@Repository
public class DistributionWorkflowDao extends AbstractDao {
    private static final String PREFIX = "sql.DistributionWorkflow.";

    public long insertRequest(DistributionRequestSaveRequest request, UserVO actor) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("request", request);
        param.put("actor", actor);
        Number id = (Number) objNotUseSession(PREFIX + "insertRequest", param);
        return id.longValue();
    }

    public int insertItem(DistributionRequestItemSnapshot item) {
        return (Integer) insert(PREFIX + "insertItem", item);
    }

    public DistributionRequestItemSnapshot resolveItem(DistributionRequestItemRef item) {
        return (DistributionRequestItemSnapshot) objNotUseSession(PREFIX + "resolveItem", item);
    }

    public DistributionRequestRecord selectRequest(long requestId) {
        return (DistributionRequestRecord) objNotUseSession(PREFIX + "selectRequest", requestId);
    }

    public DistributionRequestRecord selectRequestForUpdate(long requestId) {
        return (DistributionRequestRecord) objNotUseSession(PREFIX + "selectRequestForUpdate", requestId);
    }

    @SuppressWarnings("unchecked")
    public List<DistributionRequestItemSnapshot> selectItems(long requestId) {
        return listNotUseSession(PREFIX + "selectItems", requestId);
    }

    @SuppressWarnings("unchecked")
    public List<DistributionRequestRecord> selectRequests(
            String requestedByUserCd, String status, boolean approvalQueue,
            boolean approvedOnly, int limit, int offset) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("requestedByUserCd", requestedByUserCd);
        param.put("status", status);
        param.put("approvalQueue", approvalQueue);
        param.put("approvedOnly", approvedOnly);
        param.put("limit", limit);
        param.put("offset", offset);
        return listNotUseSession(PREFIX + "selectRequests", param);
    }

    public int replaceDraftMetadata(long requestId, String expectedStatus,
                                    DistributionRequestSaveRequest request, UserVO actor) {
        Map<String, Object> param = stateParam(requestId, expectedStatus, actor);
        param.put("request", request);
        return update(PREFIX + "replaceDraftMetadata", param);
    }

    public int deleteItems(long requestId) {
        return delete(PREFIX + "deleteItems", requestId);
    }

    public int markSubmitted(long requestId, UserVO actor) {
        return update(PREFIX + "markSubmitted", stateParam(requestId, "DRAFT", actor));
    }

    public int markApproved(long requestId, String comment, UserVO actor) {
        Map<String, Object> param = stateParam(requestId, "PENDING_APPROVAL", actor);
        param.put("comment", comment);
        return update(PREFIX + "markApproved", param);
    }

    public int markRejected(long requestId, String comment, UserVO actor) {
        Map<String, Object> param = stateParam(requestId, "PENDING_APPROVAL", actor);
        param.put("comment", comment);
        return update(PREFIX + "markRejected", param);
    }

    public int markCancelled(long requestId, String expectedStatus, UserVO actor) {
        return update(PREFIX + "markCancelled", stateParam(requestId, expectedStatus, actor));
    }

    public int insertEvent(long requestId, String fromStatus, String toStatus,
                           String eventType, String comment, UserVO actor) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("requestId", requestId);
        param.put("fromStatus", fromStatus);
        param.put("toStatus", toStatus);
        param.put("eventType", eventType);
        param.put("comment", comment);
        param.put("actor", actor);
        return (Integer) insert(PREFIX + "insertEvent", param);
    }

    public int insertOutboxHold(long requestId) {
        return (Integer) insert(PREFIX + "insertOutboxHold", requestId);
    }

    public int countOutbox(long requestId) {
        Number count = (Number) objNotUseSession(PREFIX + "countOutbox", requestId);
        return count.intValue();
    }

    private Map<String, Object> stateParam(long requestId, String expectedStatus, UserVO actor) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("requestId", requestId);
        param.put("expectedStatus", expectedStatus);
        param.put("actor", actor);
        return param;
    }
}
