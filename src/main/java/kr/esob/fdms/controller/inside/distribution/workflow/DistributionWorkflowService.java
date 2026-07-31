package kr.esob.fdms.controller.inside.distribution.workflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.fdms.commonlogic.securityacl.FileAccessDecisionVO;
import kr.esob.fdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.commonlogic.value.Constant;
import kr.esob.fdms.controller.login.UserVO;

@Service
public class DistributionWorkflowService {
    private static final int MAX_ITEMS = 200;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SUPPORTED_OBJECT_TYPES = new HashSet<String>();

    static {
        SUPPORTED_OBJECT_TYPES.add("SW");
        SUPPORTED_OBJECT_TYPES.add("SW_SUB");
    }

    private final DistributionWorkflowDao dao;
    private final SecurityAclService aclService;

    public DistributionWorkflowService(DistributionWorkflowDao dao, SecurityAclService aclService) {
        this.dao = dao;
        this.aclService = aclService;
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail create(DistributionRequestSaveRequest input) {
        UserVO actor = aclService.requireCurrentUser();
        DistributionRequestSaveRequest request = normalizeAndValidate(input);
        List<DistributionRequestItemSnapshot> snapshots = authorizeAndResolve(request.getItems());

        long requestId = dao.insertRequest(request, actor);
        saveSnapshots(requestId, snapshots);
        requireOne(dao.insertEvent(requestId, null, DistributionWorkflowStatus.DRAFT.name(),
            "CREATE", null, actor), "record distribution request creation");
        return loadDetail(requestId, actor, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail update(long requestId, DistributionRequestSaveRequest input) {
        UserVO actor = aclService.requireCurrentUser();
        DistributionRequestRecord current = requireLockedRequest(requestId);
        requireOwner(current, actor);
        requireStatus(current, DistributionWorkflowStatus.DRAFT, DistributionWorkflowStatus.REJECTED);

        DistributionRequestSaveRequest request = normalizeAndValidate(input);
        List<DistributionRequestItemSnapshot> snapshots = authorizeAndResolve(request.getItems());
        String fromStatus = current.getStatus();
        requireOne(dao.replaceDraftMetadata(requestId, fromStatus, request, actor),
            "update distribution request draft");
        dao.deleteItems(requestId);
        saveSnapshots(requestId, snapshots);
        requireOne(dao.insertEvent(requestId, fromStatus, DistributionWorkflowStatus.DRAFT.name(),
            "UPDATE_DRAFT", null, actor), "record distribution request update");
        return loadDetail(requestId, actor, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail submit(long requestId) {
        UserVO actor = aclService.requireCurrentUser();
        DistributionRequestRecord current = requireLockedRequest(requestId);
        requireOwner(current, actor);
        requireStatus(current, DistributionWorkflowStatus.DRAFT);
        validatePersistedItems(dao.selectItems(requestId));

        requireOne(dao.markSubmitted(requestId, actor), "submit distribution request");
        requireOne(dao.insertEvent(requestId, DistributionWorkflowStatus.DRAFT.name(),
            DistributionWorkflowStatus.PENDING_APPROVAL.name(), "SUBMIT", null, actor),
            "record distribution request submission");
        return loadDetail(requestId, actor, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail approve(long requestId, DistributionDecisionRequest decision) {
        UserVO actor = requireAdministrator();
        DistributionRequestRecord current = requireLockedRequest(requestId);
        requireStatus(current, DistributionWorkflowStatus.PENDING_APPROVAL);
        if (actor.getUserCd().equals(current.getRequestedByUserCd())) {
            throw DistributionWorkflowException.forbidden(
                "SELF_APPROVAL_NOT_ALLOWED", "A requester cannot approve their own distribution request.");
        }
        String comment = validateComment(decision, false);
        validatePersistedItems(dao.selectItems(requestId));

        requireOne(dao.markApproved(requestId, comment, actor), "approve distribution request");
        requireOne(dao.insertEvent(requestId, DistributionWorkflowStatus.PENDING_APPROVAL.name(),
            DistributionWorkflowStatus.APPROVED.name(), "APPROVE", comment, actor),
            "record distribution request approval");

        // This is intentionally a HOLD snapshot only. No network or external-system
        // call is made in this module. The unique request_id makes the outbox write
        // safe against a future retry implementation.
        int inserted = dao.insertOutboxHold(requestId);
        if ((inserted != 0 && inserted != 1) || dao.countOutbox(requestId) != 1) {
            throw new IllegalStateException("Unable to persist the approved distribution HOLD snapshot.");
        }
        return loadDetail(requestId, actor, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail reject(long requestId, DistributionDecisionRequest decision) {
        UserVO actor = requireAdministrator();
        DistributionRequestRecord current = requireLockedRequest(requestId);
        requireStatus(current, DistributionWorkflowStatus.PENDING_APPROVAL);
        if (actor.getUserCd().equals(current.getRequestedByUserCd())) {
            throw DistributionWorkflowException.forbidden(
                "SELF_APPROVAL_NOT_ALLOWED", "A requester cannot decide their own distribution request.");
        }
        String comment = validateComment(decision, true);
        validatePersistedItems(dao.selectItems(requestId));

        requireOne(dao.markRejected(requestId, comment, actor), "reject distribution request");
        requireOne(dao.insertEvent(requestId, DistributionWorkflowStatus.PENDING_APPROVAL.name(),
            DistributionWorkflowStatus.REJECTED.name(), "REJECT", comment, actor),
            "record distribution request rejection");
        return loadDetail(requestId, actor, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail cancel(long requestId) {
        UserVO actor = aclService.requireCurrentUser();
        DistributionRequestRecord current = requireLockedRequest(requestId);
        requireOwner(current, actor);
        requireStatus(current, DistributionWorkflowStatus.DRAFT,
            DistributionWorkflowStatus.PENDING_APPROVAL, DistributionWorkflowStatus.REJECTED);

        requireOne(dao.markCancelled(requestId, current.getStatus(), actor),
            "cancel distribution request");
        requireOne(dao.insertEvent(requestId, current.getStatus(),
            DistributionWorkflowStatus.CANCELLED.name(), "CANCEL", null, actor),
            "record distribution request cancellation");
        return loadDetail(requestId, actor, false);
    }

    @Transactional(readOnly = true)
    public DistributionRequestDetail detail(long requestId) {
        UserVO actor = aclService.requireCurrentUser();
        DistributionRequestRecord request = requireRequest(requestId);
        boolean mayOpen = isOwner(request, actor) || isAdministrator(actor)
            || DistributionWorkflowStatus.APPROVED.name().equals(request.getStatus());
        if (!mayOpen) {
            throw DistributionWorkflowException.forbidden(
                "DISTRIBUTION_REQUEST_ACCESS_DENIED", "You cannot access this distribution request.");
        }
        return loadDetail(requestId, actor, true);
    }

    @Transactional(readOnly = true)
    public List<DistributionRequestRecord> mine(String status, Integer limit, Integer offset) {
        UserVO actor = aclService.requireCurrentUser();
        return dao.selectRequests(actor.getUserCd(), normalizeStatusFilter(status), false, false,
            normalizeLimit(limit), normalizeOffset(offset));
    }

    @Transactional(readOnly = true)
    public List<DistributionRequestRecord> approvalQueue(Integer limit, Integer offset) {
        requireAdministrator();
        return dao.selectRequests(null, null, true, false,
            normalizeLimit(limit), normalizeOffset(offset));
    }

    @Transactional(readOnly = true)
    public List<DistributionRequestRecord> approved(Integer limit, Integer offset) {
        aclService.requireCurrentUser();
        List<DistributionRequestRecord> candidates = dao.selectRequests(null, null, false, true,
            normalizeLimit(limit), normalizeOffset(offset));
        List<DistributionRequestRecord> accessible = new ArrayList<DistributionRequestRecord>();
        for (DistributionRequestRecord candidate : candidates) {
            if (canViewAll(dao.selectItems(candidate.getRequestId()))) {
                accessible.add(candidate);
            }
        }
        return accessible;
    }

    private DistributionRequestDetail loadDetail(long requestId, UserVO actor, boolean requireView) {
        DistributionRequestRecord request = requireRequest(requestId);
        List<DistributionRequestItemSnapshot> items = dao.selectItems(requestId);
        if (requireView) {
            requireView(items);
        }
        DistributionRequestDetail detail = new DistributionRequestDetail();
        detail.setRequest(request);
        detail.setItems(items);
        return detail;
    }

    private void saveSnapshots(long requestId, List<DistributionRequestItemSnapshot> snapshots) {
        int lineNo = 1;
        for (DistributionRequestItemSnapshot snapshot : snapshots) {
            snapshot.setRequestId(requestId);
            snapshot.setLineNo(lineNo++);
            requireOne(dao.insertItem(snapshot), "save distribution request item snapshot");
        }
    }

    private List<DistributionRequestItemSnapshot> authorizeAndResolve(
            List<DistributionRequestItemRef> items) {
        List<DistributionRequestItemSnapshot> snapshots = new ArrayList<DistributionRequestItemSnapshot>();
        for (DistributionRequestItemRef item : items) {
            requireView(item.getObjectType(), item.getObjectId(), item.getFileNo());
            DistributionRequestItemSnapshot snapshot = dao.resolveItem(item);
            if (snapshot == null) {
                throw DistributionWorkflowException.badRequest(
                    "DISTRIBUTION_ITEM_NOT_FOUND", "A selected technical-data file no longer exists.");
            }
            if (!item.getObjectType().equals(snapshot.getObjectType())
                    || !item.getObjectId().equals(snapshot.getObjectId())
                    || !item.getFileNo().equals(snapshot.getFileNo())) {
                throw new IllegalStateException("Resolved distribution item does not match its identifiers.");
            }
            snapshots.add(snapshot);
        }
        return snapshots;
    }

    private void validatePersistedItems(List<DistributionRequestItemSnapshot> items) {
        if (items == null || items.isEmpty()) {
            throw DistributionWorkflowException.conflict(
                "DISTRIBUTION_ITEMS_REQUIRED", "The distribution request has no items.");
        }
        for (DistributionRequestItemSnapshot saved : items) {
            requireView(saved.getObjectType(), saved.getObjectId(), saved.getFileNo());
            DistributionRequestItemRef ref = new DistributionRequestItemRef();
            ref.setObjectType(saved.getObjectType());
            ref.setObjectId(saved.getObjectId());
            ref.setFileNo(saved.getFileNo());
            DistributionRequestItemSnapshot current = dao.resolveItem(ref);
            if (current == null || !sameSourceSnapshot(saved, current)) {
                throw DistributionWorkflowException.conflict(
                    "DISTRIBUTION_ITEM_CHANGED",
                    "A selected technical-data file changed after the request was saved. Update the draft and submit again.");
            }
        }
    }

    private boolean sameSourceSnapshot(DistributionRequestItemSnapshot saved,
                                       DistributionRequestItemSnapshot current) {
        return Objects.equals(saved.getObjectType(), current.getObjectType())
            && Objects.equals(saved.getObjectId(), current.getObjectId())
            && Objects.equals(saved.getFileNo(), current.getFileNo())
            && Objects.equals(saved.getMaterialNo(), current.getMaterialNo())
            && Objects.equals(saved.getMaterialName(), current.getMaterialName())
            && Objects.equals(saved.getOriginalFileName(), current.getOriginalFileName())
            && Objects.equals(saved.getFileSize(), current.getFileSize())
            && Objects.equals(saved.getGradeCd(), current.getGradeCd());
    }

    private void requireView(List<DistributionRequestItemSnapshot> items) {
        if (items == null || items.isEmpty()) {
            throw DistributionWorkflowException.conflict(
                "DISTRIBUTION_ITEMS_REQUIRED", "The distribution request has no items.");
        }
        for (DistributionRequestItemSnapshot item : items) {
            requireView(item.getObjectType(), item.getObjectId(), item.getFileNo());
        }
    }

    private boolean canViewAll(List<DistributionRequestItemSnapshot> items) {
        if (items == null || items.isEmpty()) return false;
        for (DistributionRequestItemSnapshot item : items) {
            FileAccessDecisionVO decision = aclService.checkAccess(accessRequest(
                item.getObjectType(), item.getObjectId(), item.getFileNo()));
            if (decision == null || !decision.isAllowed()) return false;
        }
        return true;
    }

    private void requireView(String objectType, String objectId, String fileNo) {
        aclService.requireAccess(accessRequest(objectType, objectId, fileNo));
    }

    private FileAccessRequest accessRequest(String objectType, String objectId, String fileNo) {
        FileAccessRequest access = new FileAccessRequest();
        access.setActionCd(SecurityAclService.VIEW);
        access.setObjectType(objectType);
        access.setObjectId(objectId);
        access.setFileNo(fileNo);
        return access;
    }

    private DistributionRequestSaveRequest normalizeAndValidate(DistributionRequestSaveRequest input) {
        if (input == null) {
            throw DistributionWorkflowException.badRequest(
                "DISTRIBUTION_REQUEST_REQUIRED", "Distribution request data is required.");
        }
        String title = trim(input.getTitle());
        if (title.isEmpty() || title.length() > 200) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_TITLE", "Title is required and must be 200 characters or fewer.");
        }
        String purpose = trim(input.getPurpose());
        if (purpose.length() > 2000) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_PURPOSE", "Purpose must be 2,000 characters or fewer.");
        }
        if (input.getItems() == null || input.getItems().isEmpty()
                || input.getItems().size() > MAX_ITEMS) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_ITEMS", "Between 1 and 200 technical-data files are required.");
        }

        List<DistributionRequestItemRef> items = new ArrayList<DistributionRequestItemRef>();
        Set<String> duplicates = new HashSet<String>();
        for (DistributionRequestItemRef source : input.getItems()) {
            DistributionRequestItemRef item = normalizeItem(source);
            String key = item.getObjectType() + '\u0000' + item.getObjectId() + '\u0000' + item.getFileNo();
            if (!duplicates.add(key)) {
                throw DistributionWorkflowException.badRequest(
                    "DUPLICATE_DISTRIBUTION_ITEM", "The same technical-data file cannot be selected twice.");
            }
            items.add(item);
        }
        DistributionRequestSaveRequest normalized = new DistributionRequestSaveRequest();
        normalized.setTitle(title);
        normalized.setPurpose(purpose);
        normalized.setItems(items);
        return normalized;
    }

    private DistributionRequestItemRef normalizeItem(DistributionRequestItemRef source) {
        if (source == null) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_ITEM", "Each distribution item is required.");
        }
        String objectType = trim(source.getObjectType()).toUpperCase(Locale.ROOT);
        String objectId = trim(source.getObjectId());
        String fileNo = trim(source.getFileNo());
        if (!SUPPORTED_OBJECT_TYPES.contains(objectType)) {
            throw DistributionWorkflowException.badRequest(
                "UNSUPPORTED_DISTRIBUTION_OBJECT_TYPE", "Only SW and SW_SUB files are supported.");
        }
        if (!isSafeIdentifier(objectId, 128) || !isSafeIdentifier(fileNo, 50)) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_ITEM_IDENTIFIER", "Object ID and file number are invalid.");
        }
        DistributionRequestItemRef normalized = new DistributionRequestItemRef();
        normalized.setObjectType(objectType);
        normalized.setObjectId(objectId);
        normalized.setFileNo(fileNo);
        return normalized;
    }

    private boolean isSafeIdentifier(String value, int maxLength) {
        if (value.isEmpty() || value.length() > maxLength) return false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || ch == '-' || ch == '_' || ch == '.')) return false;
        }
        return true;
    }

    private String validateComment(DistributionDecisionRequest decision, boolean required) {
        String comment = decision == null ? "" : trim(decision.getComment());
        if (required && comment.isEmpty()) {
            throw DistributionWorkflowException.badRequest(
                "REJECTION_COMMENT_REQUIRED", "A rejection reason is required.");
        }
        if (comment.length() > 1000) {
            throw DistributionWorkflowException.badRequest(
                "DECISION_COMMENT_TOO_LONG", "Decision comment must be 1,000 characters or fewer.");
        }
        return comment;
    }

    private DistributionRequestRecord requireRequest(long requestId) {
        if (requestId <= 0) throw DistributionWorkflowException.notFound();
        DistributionRequestRecord request = dao.selectRequest(requestId);
        if (request == null) throw DistributionWorkflowException.notFound();
        return request;
    }

    private DistributionRequestRecord requireLockedRequest(long requestId) {
        if (requestId <= 0) throw DistributionWorkflowException.notFound();
        DistributionRequestRecord request = dao.selectRequestForUpdate(requestId);
        if (request == null) throw DistributionWorkflowException.notFound();
        return request;
    }

    private void requireOwner(DistributionRequestRecord request, UserVO actor) {
        if (!isOwner(request, actor)) {
            throw DistributionWorkflowException.forbidden(
                "DISTRIBUTION_REQUEST_OWNER_REQUIRED", "Only the requester can change this request.");
        }
    }

    private boolean isOwner(DistributionRequestRecord request, UserVO actor) {
        return actor != null && actor.getUserCd() != null
            && actor.getUserCd().equals(request.getRequestedByUserCd());
    }

    private UserVO requireAdministrator() {
        UserVO actor = aclService.requireCurrentUser();
        if (!isAdministrator(actor)) {
            throw new AccessDeniedException("Distribution approval requires an administrator role.");
        }
        return actor;
    }

    private boolean isAdministrator(UserVO actor) {
        return actor != null && Constant.GROUP_CD_ADMIN.equals(actor.getRoleGroup());
    }

    private void requireStatus(DistributionRequestRecord request,
                               DistributionWorkflowStatus... allowed) {
        for (DistributionWorkflowStatus status : allowed) {
            if (status.name().equals(request.getStatus())) return;
        }
        throw DistributionWorkflowException.conflict(
            "INVALID_DISTRIBUTION_STATUS_TRANSITION",
            "This action is not allowed while the request is " + request.getStatus() + '.');
    }

    private String normalizeStatusFilter(String status) {
        String normalized = trim(status).toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) return null;
        try {
            return DistributionWorkflowStatus.valueOf(normalized).name();
        } catch (IllegalArgumentException exception) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_STATUS", "Unknown distribution request status.");
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) return 50;
        if (limit.intValue() < 1 || limit.intValue() > MAX_PAGE_SIZE) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_PAGE_LIMIT", "Limit must be between 1 and 100.");
        }
        return limit.intValue();
    }

    private int normalizeOffset(Integer offset) {
        if (offset == null) return 0;
        if (offset.intValue() < 0) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_PAGE_OFFSET", "Offset cannot be negative.");
        }
        return offset.intValue();
    }

    private void requireOne(int affectedRows, String operation) {
        if (affectedRows != 1) {
            throw new IllegalStateException("Unable to " + operation + '.');
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
