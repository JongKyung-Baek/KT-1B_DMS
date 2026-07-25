package kr.esob.fdms.commonlogic.viewer;

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
public class ViewerTicketService {
    private final ViewerTicketDao dao;
    private final SecurityAclService aclService;

    public ViewerTicketService(ViewerTicketDao dao, SecurityAclService aclService) {
        this.dao = dao;
        this.aclService = aclService;
    }

    @Transactional
    public String issue(CommonViewerParam resource, String cachedFileName) {
        if (resource == null || isBlank(resource.getObjectId()) || !isSafePdfName(cachedFileName)) {
            throw new IllegalArgumentException("뷰어 티켓 자료가 올바르지 않습니다.");
        }
        UserVO actor = aclService.requireCurrentUser();
        ViewerTicketVO ticket = new ViewerTicketVO();
        ticket.setDisposableKey(UUID.randomUUID().toString().replace("-", ""));
        ticket.setObjectType(aclService.normalizeObjectType(resource.getObjectType()));
        ticket.setObjectId(resource.getObjectId());
        ticket.setFileNo(isBlank(resource.getFileNo()) ? "*" : resource.getFileNo());
        ticket.setFileName(cachedFileName);
        ticket.setUserCd(actor.getUserCd());
        ticket.setSessionId(currentSessionId());
        dao.deleteExpired();
        dao.insertTicket(ticket);
        return ticket.getDisposableKey();
    }

    public String resolve(String key) {
        if (isBlank(key) || !key.matches("[0-9a-fA-F]{32}")) {
            throw new AccessDeniedException("유효하지 않은 뷰어 티켓입니다.");
        }
        UserVO actor = aclService.requireCurrentUser();
        String sessionId = currentSessionId();
        ViewerTicketVO ticket = dao.selectValid(key, actor.getUserCd(), sessionId);
        if (ticket == null || !isSafePdfName(ticket.getFileName())) {
            throw new AccessDeniedException("만료되었거나 소유자가 다른 뷰어 티켓입니다.");
        }
        CommonViewerParam resource = new CommonViewerParam();
        resource.setObjectType(ticket.getObjectType());
        resource.setObjectId(ticket.getObjectId());
        resource.setFileNo(ticket.getFileNo());
        // Re-check current clearance so revocation immediately affects an issued ticket.
        kr.esob.fdms.commonlogic.securityacl.FileAccessRequest access =
            new kr.esob.fdms.commonlogic.securityacl.FileAccessRequest();
        access.setActionCd(SecurityAclService.VIEW);
        access.setObjectType(ticket.getObjectType());
        access.setObjectId(ticket.getObjectId());
        access.setFileNo(ticket.getFileNo());
        aclService.requireAccess(access);
        if (dao.markUsed(key, actor.getUserCd(), sessionId) != 1) {
            throw new AccessDeniedException("Viewer ticket was already used or expired.");
        }
        return ticket.getFileName();
    }

    private String currentSessionId() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes)) {
            throw new AccessDeniedException("세션이 없습니다.");
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (request.getSession(false) == null) {
            throw new AccessDeniedException("세션이 없습니다.");
        }
        return request.getSession(false).getId();
    }

    private boolean isSafePdfName(String value) {
        return value != null && value.matches("[A-Za-z0-9._-]+\\.pdf");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
