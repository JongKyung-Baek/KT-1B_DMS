package kr.esob.fdms.commonlogic.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kr.esob.fdms.commonlogic.securityacl.SecurityAclService;
import kr.esob.fdms.controller.login.UserVO;

@Service
public class PrintAuditService {
    private final PrintAuditDao printAuditDao;
    private final CommonViewerDao viewerDao;
    private final SecurityAclService aclService;

    public PrintAuditService(PrintAuditDao printAuditDao, CommonViewerDao viewerDao, SecurityAclService aclService) {
        this.printAuditDao = printAuditDao;
        this.viewerDao = viewerDao;
        this.aclService = aclService;
    }

    @Transactional
    public String start(CommonViewerParam source) {
        List<CommonViewerParam> items = new ArrayList<CommonViewerParam>();
        items.add(source);
        return start(items);
    }

    @Transactional
    public String start(List<CommonViewerParam> sources) {
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("출력할 자료가 없습니다.");
        }
        UserVO actor = aclService.requireCurrentUser();
        String printJobId = UUID.randomUUID().toString();
        CommonViewerParam first = sources.get(0);

        PrintJobVO job = new PrintJobVO();
        job.setPrintJobId(printJobId);
        job.setStatusCd("STARTED");
        job.setActorUserCd(actor.getUserCd());
        job.setObjectType(sources.size() > 1 ? "MERGE" : normalizeObjectType(first.getObjectType()));
        job.setObjectId(sources.size() > 1 ? printJobId : first.getObjectId());
        job.setFileNo(sources.size() > 1 ? "*" : defaultFileNo(first.getFileNo()));
        job.setRequestNo(first.getRequestNo());
        requireSingleRow(printAuditDao.insertJob(job, actor, currentClientIp()), "출력 이력 생성");

        int itemSeq = 1;
        for (CommonViewerParam source : sources) {
            PrintJobItemVO item = new PrintJobItemVO();
            item.setPrintJobId(printJobId);
            item.setItemSeq(Integer.valueOf(itemSeq++));
            item.setObjectType(normalizeObjectType(source.getObjectType()));
            item.setObjectId(source.getObjectId());
            item.setFileNo(defaultFileNo(source.getFileNo()));
            item.setRequestNo(source.getRequestNo());
            item.setRequestType(normalizeRequestType(source.getRequestType()));
            item.setCountRequiredYn(requiresLegacyPrintCount(source) ? "Y" : "N");
            requireSingleRow(printAuditDao.insertItem(item), "출력 대상 이력 생성");
        }

        aclService.recordPrintResult(actor, "STARTED", null, job.getObjectType(), job.getObjectId(),
            job.getFileNo(), job.getRequestNo(), "출력 작업 발급", "{\"printJobId\":\"" + printJobId + "\"}");
        return printJobId;
    }

    @Transactional
    public PrintJobVO complete(PrintResultParam result) {
        if (result == null || isBlank(result.getPrintJobId()) || isBlank(result.getStatusCd())) {
            throw new IllegalArgumentException("출력 작업 ID와 결과 상태는 필수입니다.");
        }
        UserVO actor = aclService.requireCurrentUser();
        result.setPrintJobId(result.getPrintJobId().trim());
        PrintJobVO job = printAuditDao.selectJobForUpdate(result.getPrintJobId());
        if (job == null) {
            throw new IllegalArgumentException("출력 작업을 찾을 수 없습니다.");
        }
        if (!actor.getUserCd().equals(job.getActorUserCd())) {
            throw new AccessDeniedException("본인이 시작한 출력 작업만 완료할 수 있습니다.");
        }

        String status = result.getStatusCd().trim().toUpperCase(Locale.ROOT);
        if (!"SUCCESS".equals(status) && !"FAILED".equals(status) && !"CANCELLED".equals(status)) {
            throw new IllegalArgumentException("지원하지 않는 출력 결과입니다.");
        }
        result.setStatusCd(status);

        if (!"STARTED".equals(job.getStatusCd())) {
            if (status.equals(job.getStatusCd())) {
                if ("SUCCESS".equals(status) && !"Y".equals(job.getCountAppliedYn())) {
                    throw new IllegalStateException("출력 성공 상태와 횟수 반영 상태가 일치하지 않습니다.");
                }
                return job;
            }
            throw new IllegalStateException("이미 종료된 출력 작업입니다.");
        }
        if (printAuditDao.completeJob(result, actor.getUserCd()) != 1) {
            throw new IllegalStateException("출력 결과를 반영하지 못했습니다.");
        }

        List<PrintJobItemVO> items = printAuditDao.selectItems(job.getPrintJobId());
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("출력 대상 이력이 존재하지 않습니다.");
        }
        if ("SUCCESS".equals(status)) {
            for (PrintJobItemVO item : items) {
                if (!requiresLegacyPrintCount(item)) {
                    continue;
                }
                CommonViewerParam countParam = new CommonViewerParam();
                countParam.setSessionUser(actor);
                countParam.setRequestNo(item.getRequestNo());
                countParam.setObjectType(toLegacyObjectType(item.getObjectType()));
                countParam.setObjectId(item.getObjectId());
                countParam.setFileNo(item.getFileNo());
                requireSingleRow(viewerDao.updatePrintCnt(countParam), "출력 횟수 반영");
            }
            requireSingleRow(printAuditDao.markCountApplied(job.getPrintJobId(), actor.getUserCd()),
                "출력 횟수 반영 상태 저장");
        }

        String detail = "{\"printJobId\":\"" + job.getPrintJobId() + "\",\"pageCount\":"
            + numberOrNull(result.getPageCount()) + ",\"copyCount\":" + numberOrNull(result.getCopyCount()) + "}";
        aclService.recordPrintResult(actor, status, "SUCCESS".equals(status) ? null : "PRINT_" + status,
            job.getObjectType(), job.getObjectId(), job.getFileNo(), job.getRequestNo(),
            result.getErrorMessage(), detail);

        job.setStatusCd(status);
        job.setCountAppliedYn("SUCCESS".equals(status) ? "Y" : "N");
        return job;
    }

    private String toLegacyObjectType(String objectType) {
        if ("DOCUMENT".equals(objectType) || "PRODUCT_DOCUMENT".equals(objectType)) return "DOC";
        if ("PRODUCT_SW".equals(objectType)) return "SW";
        return objectType;
    }

    private String normalizeObjectType(String objectType) {
        if ("문서".equals(objectType)) return "DOCUMENT";
        if ("도면".equals(objectType)) return "DRAWING";
        return aclService.normalizeObjectType(objectType);
    }

    private String defaultFileNo(String fileNo) {
        return isBlank(fileNo) ? "*" : fileNo.trim();
    }

    private String normalizeRequestType(String requestType) {
        return isBlank(requestType) ? null : requestType.trim().toUpperCase(Locale.ROOT);
    }

    private boolean requiresLegacyPrintCount(PrintJobItemVO item) {
        if (!"Y".equals(item.getCountRequiredYn())) {
            return false;
        }
        if (isBlank(item.getRequestNo()) || isBlank(item.getFileNo()) || "*".equals(item.getFileNo())) {
            throw new IllegalStateException("배포 출력 횟수 반영에 필요한 식별자가 없습니다.");
        }
        return true;
    }

    private boolean requiresLegacyPrintCount(CommonViewerParam source) {
        String requestType = normalizeRequestType(source.getRequestType());
        if ("OBJECT".equals(requestType) || "UNREG".equals(requestType)) {
            return false;
        }
        if ("PRODUCT".equals(requestType) && isBlank(source.getRequestNo())) {
            return false;
        }
        if (isBlank(source.getRequestNo()) || isBlank(source.getFileNo())) {
            throw new IllegalArgumentException("출력 횟수 반영에 필요한 요청 및 파일 식별자가 없습니다.");
        }
        return true;
    }

    private String numberOrNull(Integer value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private String currentClientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes)) return null;
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (!isBlank(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma < 0 ? forwarded.trim() : forwarded.substring(0, comma).trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void requireSingleRow(int affectedRows, String operation) {
        if (affectedRows != 1) {
            throw new IllegalStateException(operation + " 처리 건수가 올바르지 않습니다: " + affectedRows);
        }
    }
}
