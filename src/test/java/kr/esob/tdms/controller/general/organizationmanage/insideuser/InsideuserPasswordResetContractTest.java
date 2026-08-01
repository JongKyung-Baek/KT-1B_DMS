package kr.esob.tdms.controller.general.organizationmanage.insideuser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class InsideuserPasswordResetContractTest {

    private static final Path MAPPER = Paths.get(
            "src/main/resources/sqlMaps/oracle/its/controller/general/organizationmanage/insideuser/Insideuser.xml");
    private static final Path VIEW = Paths.get(
            "src/main/webapp/WEB-INF/views/general/organizationmanage/insideuser/insideuserList.jsp");

    @Test
    void resetUpdateAlsoClearsLockAndFailedLoginState() throws Exception {
        String mapper = normalize(Files.readString(MAPPER, StandardCharsets.UTF_8));
        String reset = mapper.substring(
                mapper.indexOf("<update id=\"resetPwd\">"),
                mapper.indexOf("</update>", mapper.indexOf("<update id=\"resetPwd\">")));

        assertTrue(reset.contains("PWD_UPDATE_DT = CURRENT_TIMESTAMP"));
        assertTrue(reset.contains("LOGIN_COUNT = 0"));
        assertTrue(reset.contains("LOCK_YN = 'N'"));
        assertTrue(reset.contains("USE_YN = 'Y'"));
        assertTrue(reset.contains("DEL_YN = 'N'"));
    }

    @Test
    void browserPostsOnlyTheStableUserIdentifier() throws Exception {
        String view = Files.readString(VIEW, StandardCharsets.UTF_8);

        assertTrue(view.contains("var param = { userCd: data.userCd };"));
        assertFalse(view.contains("var param = data;"));
        assertTrue(view.contains("response.data || ''"));
    }

    private String normalize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }
}
