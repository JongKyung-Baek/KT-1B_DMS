package kr.esob.fdms.util.seed;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class PasswordStorageSqlContractTest {

    @Test
    void ownPasswordChangeUpdatesPasswordLifecycleState() throws IOException {
        String mapper = resource(
                "/sqlMaps/oracle/its/controller/login/Login.xml")
                .replaceAll("\\s+", " ");

        assertTrue(mapper.contains(
                "<update id=\"resetPwd\"> UPDATE DOCS_USER "
                        + "SET USER_PWD = #{userPwd}, "
                        + "PWD_UPDATE_DT = CURRENT_TIMESTAMP, "
                        + "LOGIN_COUNT = 0, "
                        + "LOCK_YN = 'N' "
                        + "WHERE USER_CD = #{userCd} </update>"));
    }

    private String resource(String path) throws IOException {
        try (InputStream input = PasswordStorageSqlContractTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Missing test resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
