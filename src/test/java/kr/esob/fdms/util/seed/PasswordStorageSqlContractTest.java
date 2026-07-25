package kr.esob.fdms.util.seed;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class PasswordStorageSqlContractTest {

    private static final String PBKDF2_SQL_PATTERN =
            "^pbkdf2-sha256[$][1-9][0-9]{5}[$][A-Za-z0-9_-]{21}[AQgw]"
                    + "[$][A-Za-z0-9_-]{42}[AEIMQUYcgkosw048]$";

    @Test
    void approvalRequiresPbkdf2ButMalformedPendingRequestCanBeRejected() throws IOException {
        String mapper = resource(
                "/sqlMaps/oracle/its/controller/inside/organizationmanage/approval/Approval.xml");

        assertTrue(mapper.contains(PBKDF2_SQL_PATTERN));
        assertTrue(mapper.contains(
                "#{rejectReason} IS NOT NULL AND BTRIM(#{rejectReason}) != ''"));
        assertTrue(mapper.contains("CROSS JOIN LATERAL GENERATE_SERIES"));
        assertTrue(mapper.contains("password_format_guard"));
    }

    @Test
    void requestTableLifecycleConstraintAcceptsOnlyVersionedPbkdf2() throws IOException {
        String ddl = resource("/sql/docs_user_request_ddl.sql");
        String compactDdl = ddl.replaceAll("\\s+", " ");

        assertTrue(ddl.contains(
                "DROP CONSTRAINT IF EXISTS ck_docs_user_request_password_lifecycle"));
        assertTrue(ddl.contains(PBKDF2_SQL_PATTERN));
        assertTrue(compactDdl.contains(
                "request_type = 'I' AND status_cd = 'REQUEST' "
                        + "AND user_pwd IS NOT NULL AND user_pwd ~ '" + PBKDF2_SQL_PATTERN + "'"));
        assertTrue(compactDdl.contains(
                "(request_type <> 'I' OR status_cd <> 'REQUEST') AND user_pwd IS NULL"));
        assertTrue(compactDdl.contains(
                "INCREMENT BY 1 MINVALUE 1 MAXVALUE 9999999999 NO CYCLE"));
        assertTrue(compactDdl.contains(
                "sequence_state.is_called OR existing_user.max_value >= sequence_state.last_value"));

        String[] validatedConstraints = {
                "ck_docs_user_request_type",
                "ck_docs_user_request_status",
                "ck_docs_user_request_flags",
                "ck_docs_user_request_no_format",
                "ck_docs_user_request_password_lifecycle",
                "fk_dur_insert_user",
                "fk_dur_approval_user",
                "fk_dur_target_user",
                "fk_dur_company"
        };
        for (String constraint : validatedConstraints) {
            assertTrue(ddl.contains("VALIDATE CONSTRAINT " + constraint),
                    "Constraint is not validated: " + constraint);
        }
    }

    @Test
    void approvalSqlUsesSequencesAndScopesUserLookupsToAuthenticatedApprover()
            throws IOException {
        String mapper = resource(
                "/sqlMaps/oracle/its/controller/inside/organizationmanage/approval/Approval.xml");

        assertTrue(mapper.contains("NEXTVAL('public.docs_user_cd_sequence')"));
        assertTrue(mapper.contains("NEXTVAL('public.docs_external_user_id_sequence')"));
        assertTrue(mapper.contains(
                "request_scope.approval_user_cd = #{sessionUser.userCd}"));
        assertTrue(mapper.contains(
                "AND a.approval_user_cd = #{sessionUser.userCd}"));
    }

    private String resource(String path) throws IOException {
        try (InputStream input = PasswordStorageSqlContractTest.class.getResourceAsStream(path)) {
            assertNotNull(input, "Missing test resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
