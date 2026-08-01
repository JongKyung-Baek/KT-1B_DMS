package kr.esob.tdms.controller.general.distribution.swrequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

class SwRegistrationAclContractTest {

    @Test
    void registrationRollsBackForCheckedFailuresAndBootstrapsAclAfterFiles() throws Exception {
        Method registration = SwRequestService.class.getMethod(
            "saveSwRegisterFileX2", MultipartHttpServletRequest.class);
        Transactional transactional = registration.getAnnotation(Transactional.class);
        assertTrue(transactional != null);
        assertEquals(Exception.class, transactional.rollbackFor()[0]);

        String source = new String(
            Files.readAllBytes(Paths.get(
                "src/main/java/kr/esob/tdms/controller/general/distribution/swrequest/SwRequestService.java")),
            StandardCharsets.UTF_8);
        String method = section(
            source,
            "public ResultVO saveSwRegisterFileX2",
            "private void sendRegistrationMail");

        int mainFileInsert = method.indexOf("dao.insertSwRegisterInfoFile(swRegisterPopupParam)");
        int subFileInsert = method.indexOf("saveSwSubFiles(subFiles");
        int aclBootstrap = method.indexOf(
            "securityAclService.initializeRegisteredSwAcl(swRegisterPopupParam.getObjectId())");
        int success = method.indexOf("resultVo.setSuccess(true)");

        assertTrue(mainFileInsert >= 0);
        assertTrue(mainFileInsert < subFileInsert);
        assertTrue(subFileInsert < aclBootstrap);
        assertTrue(aclBootstrap < success);
    }

    private String section(String source, String startMarker, String endMarker) {
        int start = source.indexOf(startMarker);
        assertTrue(start >= 0, "Start marker not found: " + startMarker);
        int end = source.indexOf(endMarker, start + startMarker.length());
        assertTrue(end >= 0, "End marker not found: " + endMarker);
        return source.substring(start, end);
    }
}
