package kr.esob.tdms.controller.general.organizationmanage.auditlog;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class AuditLogBrowserLifecycleContractTest {

    @Test
    void pageNavigationCannotBeTreatedAsLogoutOrInvalidateTheSession() throws Exception {
        String header = read("src/main/webapp/header.jsp");
        String service = read("src/main/java/kr/esob/tdms/controller/general/organizationmanage/auditlog/AuditLogService.java");
        String controller = read("src/main/java/kr/esob/tdms/controller/general/organizationmanage/auditlog/AuditLogController.java");

        assertFalse(header.contains("notifyLogoutOnLeave"));
        assertFalse(header.contains("clearPendingLogoutOnStay"));
        assertFalse(header.contains("addEventListener('pagehide'"));
        assertFalse(service.contains("BROWSER_LEAVE_LOGOUT_DELAY_MILLIS"));
        assertFalse(service.contains("finalizePendingBrowserLeaveLogout"));
        assertFalse(service.contains("session.invalidate()"));
        assertFalse(controller.contains("notifyLogoutOnLeave"));
        assertFalse(controller.contains("clearPendingLogoutOnStay"));
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
