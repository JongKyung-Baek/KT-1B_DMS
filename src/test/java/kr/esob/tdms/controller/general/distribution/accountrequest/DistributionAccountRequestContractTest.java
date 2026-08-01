package kr.esob.tdms.controller.general.distribution.accountrequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DistributionAccountRequestContractTest {
    @Test
    void integrationServiceHasAnExplicitSpringInjectionConstructor() {
        assertTrue(Arrays.stream(DistributionAccountIntegrationService.class.getConstructors())
            .anyMatch(constructor -> constructor.isAnnotationPresent(Autowired.class)));
    }

    @Test
    void migrationIsRepeatableAndIncludedInFreshDatabaseBuild() throws Exception {
        String ddl = read("src/main/resources/sql/distribution_account_request_ddl.sql");
        String fresh = read("src/main/resources/sql/fresh_database_migration.psql");

        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_account_request ("));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_account_request_event ("));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_account_request_nonce ("));
        assertTrue(ddl.contains("UNIQUE (source_system_id, event_id)"));
        assertTrue(ddl.contains("UNIQUE (source_system_id, correlation_id)"));
        assertTrue(ddl.contains("ALTER COLUMN target_user_name DROP NOT NULL"));
        assertTrue(ddl.contains("ALTER COLUMN target_user_email DROP NOT NULL"));
        assertTrue(ddl.contains("decided_by_user_cd          varchar(64)"));
        assertTrue(ddl.contains("ALTER COLUMN decided_by_user_cd TYPE varchar(64)"));
        assertTrue(fresh.contains("\\ir distribution_account_request_ddl.sql"));
    }

    @Test
    void decisionsNeverMutateTdmsUsersOrPasswords() throws Exception {
        String mapper = read(
            "src/main/resources/sqlMaps/oracle/its/controller/general/distribution/accountrequest/DistributionAccountRequest.xml");
        String normalized = mapper.toLowerCase();

        assertTrue(mapper.contains("UPDATE docs_distribution_account_request"));
        assertFalse(normalized.contains("update docs_user"));
        assertFalse(normalized.contains("user_pwd"));
        assertFalse(normalized.contains("lock_yn"));
        assertFalse(normalized.contains("docs_partner_user"));
        assertTrue(mapper.contains("NULLIF(#{targetUserName}, '')"));
        assertTrue(mapper.contains("NULLIF(#{targetUserEmail}, '')"));
    }

    @Test
    void typeSpecificFieldsAndSensitiveMetadataRulesAreDocumented()
            throws Exception {
        String service = read(
            "src/main/java/kr/esob/tdms/controller/general/distribution/accountrequest/DistributionAccountIntegrationService.java");
        String guide = read("docs/distribution-account-request-api.md");

        assertTrue(service.contains("DistributionAccountRequestType.REGISTER_USER.name()"));
        assertTrue(service.contains("optionalEmail(target, \"email\")"));
        assertTrue(service.contains("rejectSensitiveMetadataKeys(metadata)"));
        assertTrue(service.contains("\"password\", \"passwd\", \"pwd\", \"secret\", \"token\", \"credential\""));
        assertTrue(service.contains("\"apikey\", \"privatekey\""));
        assertTrue(service.contains("redactInternalDecisionIdentity(request)"));
        assertTrue(guide.contains("UNLOCK_ACCOUNT` and `RESET_PASSWORD`: only `targetUser.id` is required"));
        assertTrue(guide.contains("Metadata keys are checked recursively"));
        assertTrue(guide.contains(
            "Every external response, including a duplicate receipt, omits internal TDMS"));
        assertTrue(guide.contains("TDMS-user actors in\nthe external event history are also de-identified"));
        assertTrue(guide.contains(
            "The external distribution\nsystem performs the approved operation"));
    }

    @Test
    void signedExternalAndAdministratorEndpointsAreExplicitlyProtected() throws Exception {
        String security = read("src/main/java/kr/esob/tdms/config/SecurityConfig.java");
        assertTrue(security.contains("DistributionAccountIntegrationProperties.REQUEST_PATH"));
        assertTrue(security.contains("ROLE_MENU_231"));
        assertTrue(security.contains("ignoringAntMatchers"));
    }

    @Test
    void aixRunbookDocumentsHmacRegistrationSecretRotationAndReplayChecks()
            throws Exception {
        String guide = read("docs/aix73-deployment.md");

        assertTrue(guide.contains("TDMS_DISTRIBUTION_INTEGRATION_ENABLED=false"));
        assertTrue(guide.contains("TDMS_DISTRIBUTION_INTEGRATION_CLIENT_ID"));
        assertTrue(guide.contains("TDMS_DISTRIBUTION_INTEGRATION_SOURCE_SYSTEM_ID"));
        assertTrue(guide.contains("TDMS_DISTRIBUTION_INTEGRATION_SHARED_SECRET"));
        assertTrue(guide.contains("TDMS_DISTRIBUTION_INTEGRATION_ADDITIONAL_CLIENTS"));
        assertTrue(guide.contains("TDMS_DISTRIBUTION_INTEGRATION_CLOCK_SKEW_SECONDS=300"));
        assertTrue(guide.contains("TDMS_DISTRIBUTION_INTEGRATION_NONCE_RETENTION_DAYS=2"));
        assertTrue(guide.contains("openssl rand -base64 48"));
        assertTrue(guide.contains("chmod 600"));
        assertTrue(guide.contains(
            "POST /api/integrations/distribution/v1/account-requests"));
        assertTrue(guide.contains(
            "GET /api/integrations/distribution/v1/account-requests/{eventId}"));
        assertTrue(guide.contains("동일 nonce 재사용"));
        assertTrue(guide.contains("새 client ID와 새 비밀키"));
    }

    private String read(String path) throws Exception {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }
}
