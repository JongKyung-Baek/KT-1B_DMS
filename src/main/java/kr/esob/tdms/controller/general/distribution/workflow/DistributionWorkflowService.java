package kr.esob.tdms.controller.general.distribution.workflow;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.esob.tdms.commonlogic.securityacl.FileAccessRequest;
import kr.esob.tdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.tdms.commonlogic.value.Constant;
import kr.esob.tdms.controller.general.organizationmanage.partner.PartnerDirectoryService;
import kr.esob.tdms.controller.general.organizationmanage.partner.PartnerRecipient;
import kr.esob.tdms.controller.login.UserVO;

@Service
public class DistributionWorkflowService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");
    private static final int DEFAULT_DISTRIBUTION_DAYS = 7;
    private static final int MAX_DOCUMENTS = 200;
    private static final int MAX_BUNDLE_FILES = 2000;
    private static final int MAX_RECIPIENTS = 50;
    private static final int MAX_PAGE_SIZE = 100;

    private final DistributionWorkflowDao dao;
    private final SecurityAclService aclService;
    private final PartnerDirectoryService partnerDirectoryService;

    public DistributionWorkflowService(DistributionWorkflowDao dao,
            SecurityAclService aclService,
            PartnerDirectoryService partnerDirectoryService) {
        this.dao = dao;
        this.aclService = aclService;
        this.partnerDirectoryService = partnerDirectoryService;
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail create(DistributionRequestSaveRequest input) {
        UserVO actor = aclService.requireCurrentUser();
        DistributionRequestSaveRequest request = normalizeAndValidate(input);
        PartnerSelection partner = resolvePartnerSelection(request);
        DistributionApproverOption approver = requireActiveApprover(request.getApproverUserCd());
        requireDifferentRequesterAndApprover(actor, approver);
        List<DistributionRequestItemSnapshot> snapshots =
            authorizeAndResolveDocuments(request.getDocuments());

        long requestId = dao.insertRequest(request, actor, partner.company, approver);
        saveRecipients(requestId, partner.recipients);
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
        PartnerSelection partner = resolvePartnerSelection(request);
        DistributionApproverOption approver = requireActiveApprover(request.getApproverUserCd());
        requireDifferentRequesterAndApprover(actor, approver);
        List<DistributionRequestItemSnapshot> snapshots =
            authorizeAndResolveDocuments(request.getDocuments());
        String fromStatus = current.getStatus();

        requireOne(dao.replaceDraftMetadata(requestId, fromStatus, request, actor,
            partner.company, approver), "update distribution request draft");
        dao.deleteRecipients(requestId);
        dao.deleteItems(requestId);
        saveRecipients(requestId, partner.recipients);
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
        requireUnexpired(current);
        requireActiveApprover(current.getApproverUserCd());
        validatePersistedRecipients(current, dao.selectRecipients(requestId));
        validatePersistedBundles(dao.selectItems(requestId));

        requireOne(dao.markSubmitted(requestId, actor), "submit distribution request");
        requireOne(dao.insertEvent(requestId, DistributionWorkflowStatus.DRAFT.name(),
            DistributionWorkflowStatus.PENDING_APPROVAL.name(), "SUBMIT", null, actor),
            "record distribution request submission");
        return loadDetail(requestId, actor, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail approve(long requestId, DistributionDecisionRequest decision) {
        UserVO actor = aclService.requireCurrentUser();
        DistributionRequestRecord current = requireLockedRequest(requestId);
        requireStatus(current, DistributionWorkflowStatus.PENDING_APPROVAL);
        requireAssignedApprover(current, actor);
        requireUnexpired(current);
        String comment = validateComment(decision, false);
        validatePersistedRecipients(current, dao.selectRecipients(requestId));
        validatePersistedBundles(dao.selectItems(requestId));

        requireOne(dao.markApproved(requestId, comment, actor), "approve distribution request");
        requireOne(dao.insertEvent(requestId, DistributionWorkflowStatus.PENDING_APPROVAL.name(),
            DistributionWorkflowStatus.APPROVED.name(), "APPROVE", comment, actor),
            "record distribution request approval");

        int inserted = dao.insertOutboxHold(requestId);
        if ((inserted != 0 && inserted != 1) || dao.countOutbox(requestId) != 1) {
            throw new IllegalStateException("Unable to persist the approved distribution HOLD snapshot.");
        }
        return loadDetail(requestId, actor, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public DistributionRequestDetail reject(long requestId, DistributionDecisionRequest decision) {
        UserVO actor = aclService.requireCurrentUser();
        DistributionRequestRecord current = requireLockedRequest(requestId);
        requireStatus(current, DistributionWorkflowStatus.PENDING_APPROVAL);
        requireAssignedApprover(current, actor);
        requireUnexpired(current);
        String comment = validateComment(decision, true);

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
        requireUnexpired(current);

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
        boolean activeApproved = DistributionWorkflowStatus.APPROVED.name().equals(request.getStatus())
            && isActiveDistributionPeriod(request);
        boolean mayOpen = isOwner(request, actor) || isAssignedApprover(request, actor)
            || isAdministrator(actor) || activeApproved;
        if (!mayOpen) {
            throw DistributionWorkflowException.forbidden(
                "DISTRIBUTION_REQUEST_ACCESS_DENIED", "You cannot access this distribution request.");
        }
        return loadDetail(requestId, actor, true);
    }

    @Transactional(readOnly = true)
    public List<DistributionRequestRecord> mine(String status, Integer limit, Integer offset) {
        UserVO actor = aclService.requireCurrentUser();
        return dao.selectRequests(actor.getUserCd(), null, normalizeStatusFilter(status), false, false,
            normalizeLimit(limit), normalizeOffset(offset));
    }

    @Transactional(readOnly = true)
    public List<DistributionRequestRecord> approvalQueue(Integer limit, Integer offset) {
        UserVO actor = aclService.requireCurrentUser();
        if (dao.selectApprover(actor.getUserCd()) == null) {
            throw new AccessDeniedException("Distribution approval role is required.");
        }
        return dao.selectRequests(null, actor.getUserCd(), null, true, false,
            normalizeLimit(limit), normalizeOffset(offset));
    }

    @Transactional(readOnly = true)
    public List<DistributionRequestRecord> approved(Integer limit, Integer offset) {
        UserVO actor = aclService.requireCurrentUser();
        return dao.selectAccessibleApprovedRequests(actor.getUserCd(),
            normalizeLimit(limit), normalizeOffset(offset));
    }

    /** Compatibility bridge for technical-data inquiry links that still pass file refs. */
    @Transactional(readOnly = true)
    public List<DistributionRequestItemSnapshot> selectionPreview(
            List<DistributionRequestItemRef> sourceItems) {
        aclService.requireCurrentUser();
        if (sourceItems == null || sourceItems.isEmpty()) {
            return new ArrayList<DistributionRequestItemSnapshot>();
        }
        if (sourceItems.size() > MAX_DOCUMENTS) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_DOCUMENTS", "No more than 200 technical-data documents may be selected.");
        }
        List<DistributionRequestDocumentRef> documents = new ArrayList<DistributionRequestDocumentRef>();
        Set<String> seen = new HashSet<String>();
        for (DistributionRequestItemRef source : sourceItems) {
            String objectId = safeObjectId(source == null ? null : source.getObjectId());
            if (seen.add(objectId)) {
                DistributionRequestDocumentRef document = new DistributionRequestDocumentRef();
                document.setObjectId(objectId);
                documents.add(document);
            }
        }
        return authorizeAndResolveDocuments(documents);
    }

    /** Returns only documents whose complete main+auxiliary file bundle is viewable. */
    @Transactional(readOnly = true)
    public List<DistributionDocumentBundle> catalog(String treeCd) {
        UserVO actor = aclService.requireCurrentUser();
        String normalizedTreeCd = trim(treeCd).toUpperCase(Locale.ROOT);
        if (!isSafeIdentifier(normalizedTreeCd, 50)) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_TREE", "A valid technical-data category is required.");
        }

        Map<String, List<DistributionRequestItemSnapshot>> grouped =
            groupByObjectId(dao.selectAccessibleCatalogItems(normalizedTreeCd, actor.getUserCd()));
        List<DistributionDocumentBundle> result = new ArrayList<DistributionDocumentBundle>();
        for (Map.Entry<String, List<DistributionRequestItemSnapshot>> entry : grouped.entrySet()) {
            List<DistributionRequestItemSnapshot> files = entry.getValue();
            if (!hasExactlyOneMain(files)) {
                continue;
            }
            validateBundleMetadata(entry.getKey(), files);
            result.add(toBundle(files));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DistributionPartnerOption> partners() {
        List<PartnerRecipient> source = partnerDirectoryService.listActiveRecipients();
        Map<Long, DistributionPartnerOption> unique =
            new LinkedHashMap<Long, DistributionPartnerOption>();
        for (PartnerRecipient recipient : source) {
            if (recipient == null || recipient.getPartnerCompanyId() == null) continue;
            if (!unique.containsKey(recipient.getPartnerCompanyId())) {
                DistributionPartnerOption option = new DistributionPartnerOption();
                option.setPartnerCompanyId(recipient.getPartnerCompanyId());
                option.setCode(recipient.getCompanyCode());
                option.setName(recipient.getCompanyName());
                unique.put(recipient.getPartnerCompanyId(), option);
            }
        }
        return new ArrayList<DistributionPartnerOption>(unique.values());
    }

    @Transactional(readOnly = true)
    public List<DistributionRecipientOption> recipients(long partnerCompanyId) {
        if (partnerCompanyId <= 0) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_PARTNER_IDENTIFIER", "A valid partner company is required.");
        }
        List<DistributionRecipientOption> result = new ArrayList<DistributionRecipientOption>();
        for (PartnerRecipient source : partnerDirectoryService.listActiveRecipients(partnerCompanyId)) {
            result.add(toRecipientOption(source));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DistributionApproverOption> approvers() {
        aclService.requireCurrentUser();
        return dao.selectApprovers();
    }

    @Transactional(rollbackFor = Exception.class)
    public int expireElapsedRequests() {
        return dao.expireElapsedRequests();
    }

    private DistributionRequestDetail loadDetail(long requestId, UserVO actor, boolean requireView) {
        DistributionRequestRecord request = requireRequest(requestId);
        List<DistributionRequestItemSnapshot> items = dao.selectItems(requestId);
        if (requireView) requireView(items);
        DistributionRequestDetail detail = new DistributionRequestDetail();
        detail.setRequest(request);
        detail.setRecipients(dao.selectRecipients(requestId));
        detail.setItems(items);
        detail.setDocuments(toBundles(items));
        detail.setEvents(dao.selectEvents(requestId));
        return detail;
    }

    private void saveRecipients(long requestId, List<PartnerRecipient> recipients) {
        int lineNo = 1;
        for (PartnerRecipient source : recipients) {
            DistributionRequestRecipientSnapshot snapshot =
                new DistributionRequestRecipientSnapshot();
            snapshot.setRequestId(requestId);
            snapshot.setLineNo(lineNo++);
            snapshot.setPartnerCompanyId(source.getPartnerCompanyId());
            snapshot.setPartnerUserId(source.getPartnerUserId());
            snapshot.setUserName(source.getUserName());
            snapshot.setEmail(source.getEmail());
            snapshot.setPhone(source.getPhone());
            snapshot.setRepresentativeYn(normalizeYn(source.getRepresentativeYn()));
            requireOne(dao.insertRecipient(snapshot), "save distribution recipient snapshot");
        }
    }

    private void saveSnapshots(long requestId, List<DistributionRequestItemSnapshot> snapshots) {
        int lineNo = 1;
        for (DistributionRequestItemSnapshot snapshot : snapshots) {
            snapshot.setRequestId(requestId);
            snapshot.setLineNo(lineNo++);
            requireOne(dao.insertItem(snapshot), "save distribution request item snapshot");
        }
    }

    private List<DistributionRequestItemSnapshot> authorizeAndResolveDocuments(
            List<DistributionRequestDocumentRef> documents) {
        List<DistributionRequestItemSnapshot> snapshots =
            new ArrayList<DistributionRequestItemSnapshot>();
        int documentLineNo = 1;
        for (DistributionRequestDocumentRef document : documents) {
            List<DistributionRequestItemSnapshot> files =
                dao.resolveDocumentFiles(document.getObjectId());
            validateBundleMetadata(document.getObjectId(), files);
            int fileLineNo = 1;
            for (DistributionRequestItemSnapshot file : files) {
                requireView(file.getObjectType(), file.getObjectId(), file.getFileNo());
                validateResolvedSnapshot(file);
                file.setDocumentLineNo(documentLineNo);
                file.setFileLineNo(fileLineNo++);
                snapshots.add(file);
                if (snapshots.size() > MAX_BUNDLE_FILES) {
                    throw DistributionWorkflowException.badRequest(
                        "DISTRIBUTION_FILE_LIMIT_EXCEEDED",
                        "The selected documents contain more than 2,000 files.");
                }
            }
            documentLineNo++;
        }
        return snapshots;
    }

    private void validateBundleMetadata(String expectedObjectId,
            List<DistributionRequestItemSnapshot> files) {
        if (files == null || files.isEmpty()) {
            throw DistributionWorkflowException.badRequest(
                "DISTRIBUTION_DOCUMENT_NOT_FOUND",
                "A selected technical-data document no longer exists.");
        }
        if (!hasExactlyOneMain(files)) {
            throw DistributionWorkflowException.conflict(
                "DISTRIBUTION_MAIN_FILE_INVALID",
                "A technical-data document must contain exactly one main file.");
        }
        Set<String> fileKeys = new HashSet<String>();
        for (DistributionRequestItemSnapshot file : files) {
            if (file == null || !expectedObjectId.equals(file.getObjectId())
                    || !("SW".equals(file.getObjectType()) || "SW_SUB".equals(file.getObjectType()))
                    || !fileKeys.add(file.getObjectType() + '\u0000' + file.getFileNo())) {
                throw DistributionWorkflowException.conflict(
                    "DISTRIBUTION_DOCUMENT_BUNDLE_INVALID",
                    "The technical-data file bundle is inconsistent.");
            }
        }
    }

    private void validateResolvedSnapshot(DistributionRequestItemSnapshot snapshot) {
        if (trim(snapshot.getMaterialNo()).isEmpty()
                || snapshot.getMaterialNo().length() > 200
                || snapshot.getMaterialName() == null
                || snapshot.getMaterialName().length() > 500
                || trim(snapshot.getOriginalFileName()).isEmpty()
                || snapshot.getOriginalFileName().length() > 500
                || snapshot.getFileSize() == null
                || snapshot.getFileSize().longValue() < 0
                || trim(snapshot.getGradeCd()).isEmpty()
                || snapshot.getGradeCd().length() > 50
                || trim(snapshot.getTreeCd()).isEmpty()
                || snapshot.getTreeCd().length() > 50
                || trim(snapshot.getTreeNm()).isEmpty()
                || snapshot.getTreeNm().length() > 500
                || trim(snapshot.getParentTreeCd()).isEmpty()
                || snapshot.getParentTreeCd().length() > 50
                || trim(snapshot.getParentTreeNm()).isEmpty()
                || snapshot.getParentTreeNm().length() > 500) {
            throw DistributionWorkflowException.conflict(
                "DISTRIBUTION_ITEM_METADATA_INVALID",
                "A selected technical-data file has incomplete distribution metadata.");
        }
    }

    private void validatePersistedBundles(List<DistributionRequestItemSnapshot> savedItems) {
        Map<String, List<DistributionRequestItemSnapshot>> savedByObject = groupByObjectId(savedItems);
        if (savedByObject.isEmpty()) {
            throw DistributionWorkflowException.conflict(
                "DISTRIBUTION_ITEMS_REQUIRED", "The distribution request has no documents.");
        }
        for (Map.Entry<String, List<DistributionRequestItemSnapshot>> entry : savedByObject.entrySet()) {
            String objectId = entry.getKey();
            List<DistributionRequestItemSnapshot> saved = sortedFiles(entry.getValue());
            List<DistributionRequestItemSnapshot> current =
                sortedFiles(dao.resolveDocumentFiles(objectId));
            validateBundleMetadata(objectId, current);
            for (DistributionRequestItemSnapshot file : current) {
                requireView(file.getObjectType(), file.getObjectId(), file.getFileNo());
                validateResolvedSnapshot(file);
            }
            if (saved.size() != current.size()) throwChangedBundle();
            for (int index = 0; index < saved.size(); index++) {
                if (!sameSourceSnapshot(saved.get(index), current.get(index))) {
                    throwChangedBundle();
                }
            }
        }
    }

    private void throwChangedBundle() {
        throw DistributionWorkflowException.conflict(
            "DISTRIBUTION_ITEM_CHANGED",
            "A selected document file bundle changed. Update the draft and submit again.");
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
            && Objects.equals(saved.getGradeCd(), current.getGradeCd())
            && Objects.equals(saved.getTreeCd(), current.getTreeCd())
            && Objects.equals(saved.getTreeNm(), current.getTreeNm())
            && Objects.equals(saved.getParentTreeCd(), current.getParentTreeCd())
            && Objects.equals(saved.getParentTreeNm(), current.getParentTreeNm());
    }

    private void validatePersistedRecipients(DistributionRequestRecord request,
            List<DistributionRequestRecipientSnapshot> saved) {
        if (saved == null || saved.isEmpty() || saved.size() > MAX_RECIPIENTS) {
            throw DistributionWorkflowException.conflict(
                "DISTRIBUTION_RECIPIENTS_CHANGED",
                "The distribution request has no valid recipients.");
        }
        Map<Long, PartnerRecipient> active = new LinkedHashMap<Long, PartnerRecipient>();
        for (PartnerRecipient recipient :
                partnerDirectoryService.listActiveRecipients(request.getPartnerCompanyId())) {
            active.put(recipient.getPartnerUserId(), recipient);
        }
        for (DistributionRequestRecipientSnapshot snapshot : saved) {
            PartnerRecipient current = active.get(snapshot.getPartnerUserId());
            if (current == null || !sameRecipientSnapshot(snapshot, current)) {
                throw DistributionWorkflowException.conflict(
                    "DISTRIBUTION_RECIPIENTS_CHANGED",
                    "A selected partner recipient changed. Update the draft and submit again.");
            }
        }
    }

    private boolean sameRecipientSnapshot(DistributionRequestRecipientSnapshot saved,
            PartnerRecipient current) {
        return Objects.equals(saved.getPartnerCompanyId(), current.getPartnerCompanyId())
            && Objects.equals(saved.getPartnerUserId(), current.getPartnerUserId())
            && Objects.equals(saved.getUserName(), current.getUserName())
            && Objects.equals(saved.getEmail(), current.getEmail())
            && Objects.equals(saved.getPhone(), current.getPhone())
            && Objects.equals(normalizeYn(saved.getRepresentativeYn()),
                              normalizeYn(current.getRepresentativeYn()));
    }

    private void requireView(List<DistributionRequestItemSnapshot> items) {
        if (items == null || items.isEmpty()) {
            throw DistributionWorkflowException.conflict(
                "DISTRIBUTION_ITEMS_REQUIRED", "The distribution request has no documents.");
        }
        for (DistributionRequestItemSnapshot item : items) {
            requireView(item.getObjectType(), item.getObjectId(), item.getFileNo());
        }
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
        if (input.getPartnerCompanyId() == null || input.getPartnerCompanyId().longValue() <= 0) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_PARTNER_IDENTIFIER", "A partner company is required.");
        }
        if (input.getRecipientUserIds() == null || input.getRecipientUserIds().isEmpty()
                || input.getRecipientUserIds().size() > MAX_RECIPIENTS) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_RECIPIENTS", "Between 1 and 50 recipients are required.");
        }
        String approverUserCd = trim(input.getApproverUserCd());
        if (!isSafeIdentifier(approverUserCd, 64)) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_APPROVER", "An authorized approver is required.");
        }
        if (input.getDocuments() == null || input.getDocuments().isEmpty()
                || input.getDocuments().size() > MAX_DOCUMENTS) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_DOCUMENTS",
                "Between 1 and 200 technical-data documents are required.");
        }

        List<Long> recipientIds = new ArrayList<Long>();
        Set<Long> recipientDuplicates = new HashSet<Long>();
        for (Long recipientId : input.getRecipientUserIds()) {
            if (recipientId == null || recipientId.longValue() <= 0
                    || !recipientDuplicates.add(recipientId)) {
                throw DistributionWorkflowException.badRequest(
                    "INVALID_DISTRIBUTION_RECIPIENTS",
                    "Recipient identifiers must be positive and unique.");
            }
            recipientIds.add(recipientId);
        }

        List<DistributionRequestDocumentRef> documents =
            new ArrayList<DistributionRequestDocumentRef>();
        Set<String> documentDuplicates = new HashSet<String>();
        for (DistributionRequestDocumentRef source : input.getDocuments()) {
            String objectId = safeObjectId(source == null ? null : source.getObjectId());
            if (!documentDuplicates.add(objectId)) {
                throw DistributionWorkflowException.badRequest(
                    "DUPLICATE_DISTRIBUTION_DOCUMENT",
                    "The same technical-data document cannot be selected twice.");
            }
            DistributionRequestDocumentRef normalized = new DistributionRequestDocumentRef();
            normalized.setObjectId(objectId);
            documents.add(normalized);
        }

        LocalDate today = businessToday();
        LocalDate start = parseDate(input.getDistributionStartDate(), today,
            "INVALID_DISTRIBUTION_START_DATE");
        LocalDate end = parseDate(input.getDistributionEndDate(),
            start.plusDays(DEFAULT_DISTRIBUTION_DAYS), "INVALID_DISTRIBUTION_END_DATE");
        if (start.isBefore(today)) {
            throw DistributionWorkflowException.badRequest(
                "DISTRIBUTION_START_DATE_IN_PAST",
                "The distribution start date cannot be before today.");
        }
        if (end.isBefore(start)) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_PERIOD",
                "The distribution end date cannot precede the start date.");
        }

        DistributionRequestSaveRequest normalized = new DistributionRequestSaveRequest();
        normalized.setTitle(title);
        normalized.setPurpose(purpose);
        normalized.setPartnerCompanyId(input.getPartnerCompanyId());
        normalized.setRecipientUserIds(recipientIds);
        normalized.setApproverUserCd(approverUserCd);
        normalized.setDistributionStartDate(start.toString());
        normalized.setDistributionEndDate(end.toString());
        normalized.setDocuments(documents);
        return normalized;
    }

    private PartnerSelection resolvePartnerSelection(DistributionRequestSaveRequest request) {
        List<PartnerRecipient> active =
            partnerDirectoryService.listActiveRecipients(request.getPartnerCompanyId());
        Map<Long, PartnerRecipient> byId = new LinkedHashMap<Long, PartnerRecipient>();
        for (PartnerRecipient recipient : active) {
            if (recipient != null && recipient.getPartnerUserId() != null) {
                byId.put(recipient.getPartnerUserId(), recipient);
            }
        }
        List<PartnerRecipient> selected = new ArrayList<PartnerRecipient>();
        for (Long recipientId : request.getRecipientUserIds()) {
            PartnerRecipient recipient = byId.get(recipientId);
            if (recipient == null) {
                throw DistributionWorkflowException.badRequest(
                    "PARTNER_RECIPIENT_UNAVAILABLE",
                    "A selected recipient is not active in the selected partner company.");
            }
            selected.add(recipient);
        }
        if (selected.isEmpty()) {
            throw DistributionWorkflowException.badRequest(
                "PARTNER_RECIPIENT_UNAVAILABLE", "The selected partner has no active recipients.");
        }
        return new PartnerSelection(selected.get(0), selected);
    }

    private DistributionApproverOption requireActiveApprover(String userCd) {
        DistributionApproverOption approver = dao.selectApprover(trim(userCd));
        if (approver == null) {
            throw DistributionWorkflowException.badRequest(
                "DISTRIBUTION_APPROVER_UNAVAILABLE",
                "The selected user cannot approve distribution requests.");
        }
        return approver;
    }

    private void requireAssignedApprover(DistributionRequestRecord request, UserVO actor) {
        DistributionApproverOption active = actor == null ? null : dao.selectApprover(actor.getUserCd());
        if (active == null) {
            throw DistributionWorkflowException.forbidden(
                "DISTRIBUTION_APPROVAL_ROLE_REQUIRED",
                "Distribution approval permission is required.");
        }
        if (!Objects.equals(request.getApproverUserCd(), active.getApproverUserCd())) {
            throw DistributionWorkflowException.forbidden(
                "ASSIGNED_DISTRIBUTION_APPROVER_REQUIRED",
                "Only the assigned approver may decide this distribution request.");
        }
        if (Objects.equals(request.getRequestedByUserCd(), actor.getUserCd())) {
            throw DistributionWorkflowException.forbidden(
                "SELF_APPROVAL_NOT_ALLOWED",
                "A requester cannot decide their own distribution request.");
        }
    }

    private void requireDifferentRequesterAndApprover(UserVO requester,
            DistributionApproverOption approver) {
        if (requester != null && Objects.equals(requester.getUserCd(), approver.getApproverUserCd())) {
            throw DistributionWorkflowException.badRequest(
                "SELF_APPROVAL_NOT_ALLOWED",
                "Select an approver other than the requester.");
        }
    }

    private void requireUnexpired(DistributionRequestRecord request) {
        if (isExpired(request)) {
            throw DistributionWorkflowException.conflict(
                "DISTRIBUTION_PERIOD_EXPIRED", "The distribution period has already expired.");
        }
    }

    private boolean isExpired(DistributionRequestRecord request) {
        try {
            return request == null || trim(request.getDistributionEndDate()).isEmpty()
                || LocalDate.parse(request.getDistributionEndDate()).isBefore(businessToday());
        } catch (DateTimeException exception) {
            return true;
        }
    }

    private boolean isActiveDistributionPeriod(DistributionRequestRecord request) {
        try {
            if (request == null || trim(request.getDistributionStartDate()).isEmpty()
                    || trim(request.getDistributionEndDate()).isEmpty()) {
                return false;
            }
            LocalDate today = businessToday();
            LocalDate start = LocalDate.parse(request.getDistributionStartDate());
            LocalDate end = LocalDate.parse(request.getDistributionEndDate());
            return !today.isBefore(start) && !today.isAfter(end);
        } catch (DateTimeException exception) {
            return false;
        }
    }

    private List<DistributionDocumentBundle> toBundles(List<DistributionRequestItemSnapshot> items) {
        List<DistributionDocumentBundle> result = new ArrayList<DistributionDocumentBundle>();
        for (List<DistributionRequestItemSnapshot> files : groupByDocumentLine(items).values()) {
            if (!files.isEmpty()) result.add(toBundle(files));
        }
        return result;
    }

    private DistributionDocumentBundle toBundle(List<DistributionRequestItemSnapshot> source) {
        List<DistributionRequestItemSnapshot> files = sortedFiles(source);
        DistributionRequestItemSnapshot first = files.get(0);
        DistributionDocumentBundle bundle = new DistributionDocumentBundle();
        bundle.setObjectId(first.getObjectId());
        bundle.setMaterialNo(first.getMaterialNo());
        bundle.setMaterialName(first.getMaterialName());
        bundle.setTreeCd(first.getTreeCd());
        bundle.setTreeNm(first.getTreeNm());
        bundle.setParentTreeCd(first.getParentTreeCd());
        bundle.setParentTreeNm(first.getParentTreeNm());
        int main = 0;
        for (DistributionRequestItemSnapshot file : files) {
            if ("SW".equals(file.getObjectType())) main++;
        }
        bundle.setMainFileCount(main);
        bundle.setSubFileCount(files.size() - main);
        bundle.setTotalFileCount(files.size());
        bundle.setFiles(files);
        return bundle;
    }

    private Map<String, List<DistributionRequestItemSnapshot>> groupByObjectId(
            List<DistributionRequestItemSnapshot> items) {
        Map<String, List<DistributionRequestItemSnapshot>> result =
            new LinkedHashMap<String, List<DistributionRequestItemSnapshot>>();
        if (items == null) return result;
        for (DistributionRequestItemSnapshot item : items) {
            if (item == null || trim(item.getObjectId()).isEmpty()) continue;
            result.computeIfAbsent(item.getObjectId(), ignored ->
                new ArrayList<DistributionRequestItemSnapshot>()).add(item);
        }
        return result;
    }

    private Map<Integer, List<DistributionRequestItemSnapshot>> groupByDocumentLine(
            List<DistributionRequestItemSnapshot> items) {
        Map<Integer, List<DistributionRequestItemSnapshot>> result =
            new LinkedHashMap<Integer, List<DistributionRequestItemSnapshot>>();
        if (items == null) return result;
        for (DistributionRequestItemSnapshot item : items) {
            result.computeIfAbsent(Integer.valueOf(item.getDocumentLineNo()), ignored ->
                new ArrayList<DistributionRequestItemSnapshot>()).add(item);
        }
        return result;
    }

    private List<DistributionRequestItemSnapshot> sortedFiles(
            List<DistributionRequestItemSnapshot> source) {
        List<DistributionRequestItemSnapshot> result = source == null
            ? new ArrayList<DistributionRequestItemSnapshot>()
            : new ArrayList<DistributionRequestItemSnapshot>(source);
        Collections.sort(result, Comparator
            .comparing((DistributionRequestItemSnapshot item) ->
                "SW".equals(item.getObjectType()) ? 0 : 1)
            .thenComparing(item -> trim(item.getFileNo())));
        return result;
    }

    private boolean hasExactlyOneMain(List<DistributionRequestItemSnapshot> files) {
        int mainCount = 0;
        if (files != null) {
            for (DistributionRequestItemSnapshot file : files) {
                if (file != null && "SW".equals(file.getObjectType())) mainCount++;
            }
        }
        return mainCount == 1;
    }

    private DistributionRecipientOption toRecipientOption(PartnerRecipient source) {
        DistributionRecipientOption target = new DistributionRecipientOption();
        target.setPartnerCompanyId(source.getPartnerCompanyId());
        target.setPartnerUserId(source.getPartnerUserId());
        target.setUserName(source.getUserName());
        target.setEmail(source.getEmail());
        target.setPhone(source.getPhone());
        target.setRepresentativeYn(normalizeYn(source.getRepresentativeYn()));
        return target;
    }

    private String safeObjectId(String value) {
        String normalized = trim(value);
        if (!isSafeIdentifier(normalized, 128)) {
            throw DistributionWorkflowException.badRequest(
                "INVALID_DISTRIBUTION_DOCUMENT_IDENTIFIER", "The document identifier is invalid.");
        }
        return normalized;
    }

    private boolean isSafeIdentifier(String value, int maxLength) {
        if (value == null || value.isEmpty() || value.length() > maxLength) return false;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (!(Character.isLetterOrDigit(character)
                    || character == '-' || character == '_' || character == '.')) return false;
        }
        return true;
    }

    private LocalDate parseDate(String source, LocalDate defaultValue, String errorCode) {
        String value = trim(source);
        if (value.isEmpty()) return defaultValue;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeException exception) {
            throw DistributionWorkflowException.badRequest(errorCode,
                "Distribution dates must use YYYY-MM-DD format.");
        }
    }

    private LocalDate businessToday() {
        return LocalDate.now(BUSINESS_ZONE);
    }

    private String normalizeYn(String value) {
        return "Y".equalsIgnoreCase(trim(value)) ? "Y" : "N";
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

    private boolean isAssignedApprover(DistributionRequestRecord request, UserVO actor) {
        return actor != null && Objects.equals(request.getApproverUserCd(), actor.getUserCd());
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

    private static final class PartnerSelection {
        private final PartnerRecipient company;
        private final List<PartnerRecipient> recipients;

        private PartnerSelection(PartnerRecipient company, List<PartnerRecipient> recipients) {
            this.company = company;
            this.recipients = recipients;
        }
    }
}
