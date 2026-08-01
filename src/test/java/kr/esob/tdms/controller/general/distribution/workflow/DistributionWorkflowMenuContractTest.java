package kr.esob.tdms.controller.general.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class DistributionWorkflowMenuContractTest {
    private static final Path MENU_DDL = Paths.get(
        "src/main/resources/sql/distribution_workflow_menu_ddl.sql");
    private static final Path FRESH_MIGRATION = Paths.get(
        "src/main/resources/sql/fresh_database_migration.psql");
    private static final Path INTERNAL_ONLY_CLEANUP = Paths.get(
        "src/main/resources/sql/internal_only_cleanup_ddl.sql");
    private static final Path SECURITY = Paths.get(
        "src/main/java/kr/esob/tdms/config/SecurityConfig.java");
    private static final Path KOREAN_FEATURES = Paths.get(
        "src/main/webapp/messages/feature.properties");
    private static final Path ENGLISH_FEATURES = Paths.get(
        "src/main/webapp/messages/feature_en.properties");
    private static final Path RESOURCE_KOREAN_MESSAGES = Paths.get(
        "src/main/resources/messages/message_ko.properties");
    private static final Path RESOURCE_DEFAULT_MESSAGES = Paths.get(
        "src/main/resources/messages/message.properties");
    private static final Path RESOURCE_ENGLISH_MESSAGES = Paths.get(
        "src/main/resources/messages/message_en.properties");
    private static final Path WEBAPP_KOREAN_MESSAGES = Paths.get(
        "src/main/webapp/messages/message_ko.properties");
    private static final Path WEBAPP_DEFAULT_MESSAGES = Paths.get(
        "src/main/webapp/messages/message.properties");
    private static final Path WEBAPP_KOREAN_KR_MESSAGES = Paths.get(
        "src/main/webapp/messages/message_ko_KR.properties");
    private static final Path WEBAPP_ENGLISH_MESSAGES = Paths.get(
        "src/main/webapp/messages/message_en.properties");

    @Test
    void workflowMenusUseReservedIdsAndLiveUnderDistributionRoot()
            throws Exception {
        String ddl = read(MENU_DDL);

        assertTrue(ddl.contains("'MENU_229', 'ROOT', '기술자료배포'"));
        assertTrue(ddl.contains("'menu.technicalDataDistribution', '1', 'T'"));
        assertTrue(ddl.contains(
            "'/general/distribution/workflow/', 5, 'root'"));
        assertTrue(ddl.contains("'ROLE_MENU_229'"));
        assertTrue(ddl.contains(
            "('ko', 'menu.technicalDataDistribution', '기술자료배포')"));
        assertTrue(ddl.contains(
            "('en', 'menu.technicalDataDistribution', "
                + "'Technical Data Distribution')"));

        assertTrue(ddl.contains("'MENU_226', 'MENU_229', '배포요청'"));
        assertTrue(ddl.contains(
            "'/general/distribution/workflow/requests/**', 1, 'leaf'"));
        assertTrue(ddl.contains("'ROLE_MENU_226'"));

        assertTrue(ddl.contains("'MENU_227', 'MENU_229', '배포승인'"));
        assertTrue(ddl.contains(
            "'/general/distribution/workflow/approval/**', 2, 'leaf'"));
        assertTrue(ddl.contains("'ROLE_MENU_227'"));

        assertTrue(ddl.contains(
            "'MENU_228', 'MENU_229', '승인목록'"));
        assertTrue(ddl.contains(
            "'/general/distribution/workflow/approved/**', 3, 'leaf'"));
        assertTrue(ddl.contains("'ROLE_MENU_228'"));
    }

    @Test
    void viewerAudienceIsInheritedAndApprovalUsesDedicatedRoleGroup()
            throws Exception {
        String ddl = read(MENU_DDL);

        assertTrue(ddl.contains("WHERE source.role_cd = 'ROLE_MENU_220'"));
        assertTrue(ddl.contains(
            "VALUES ('ROLE_MENU_229'), ('ROLE_MENU_226'), ('ROLE_MENU_228')"));
        assertTrue(ddl.contains(
            "('RG_001', 'ROLE_MENU_229', 'SYSTEM', 'SYSTEM'"));
        assertTrue(ddl.contains(
            "('RG_001', 'ROLE_MENU_227', 'SYSTEM', 'SYSTEM'"));
        assertTrue(ddl.contains("'RG_012', '배포승인자', 'USER'"));
        assertTrue(ddl.contains(
            "('RG_012', 'ROLE_MENU_227', 'SYSTEM', 'SYSTEM'"));
        assertTrue(ddl.contains("role_cd = 'ROLE_MENU_227'"));
        assertTrue(ddl.contains("group_cd NOT IN ('RG_001', 'RG_012')"));
        assertTrue(ddl.contains("INSERT INTO docs_role_mapping"));
        assertTrue(ddl.contains("ON CONFLICT (group_cd, menu_url) DO UPDATE"));
    }

    @Test
    void workflowMenuMigrationIsPartOfEveryFreshDatabaseBuild()
            throws Exception {
        String manifest = read(FRESH_MIGRATION);
        String cleanup = read(INTERNAL_ONLY_CLEANUP);
        assertTrue(manifest.contains(
            "\\ir distribution_workflow_menu_ddl.sql"));
        assertTrue(manifest.contains(
            "Applying technical-data distribution menu hierarchy"));
        assertTrue(manifest.indexOf("\\ir distribution_workflow_ddl.sql")
            < manifest.indexOf("\\ir distribution_workflow_menu_ddl.sql"));
        assertTrue(manifest.indexOf("\\ir distribution_workflow_menu_ddl.sql")
            < manifest.indexOf("\\ir internal_only_cleanup_ddl.sql"));
        assertTrue(cleanup.contains("'MENU_229'"));
        assertTrue(cleanup.contains("five current navigation roots"));
    }

    @Test
    void approvalEndpointsHaveAnExplicitAdministratorAuthorityBoundary()
            throws Exception {
        String security = read(SECURITY);

        assertTrue(security.contains(
            "\"/general/distribution/workflow/approval/**\""));
        assertTrue(security.contains(
            "\"/general/distribution/workflow/api/approval-queue\""));
        assertTrue(security.contains(
            "\"/general/distribution/workflow/api/requests/*/approve\""));
        assertTrue(security.contains(
            "\"/general/distribution/workflow/api/requests/*/reject\""));
        assertTrue(security.contains(".hasAuthority(\"ROLE_MENU_227\")"));
        assertTrue(security.contains(
            "\"/general/distribution/workflow/api/**\").authenticated()"));
    }

    @Test
    void technicalListIntegrationMessagesExistInBothLanguages()
            throws Exception {
        String korean = read(KOREAN_FEATURES);
        String english = read(ENGLISH_FEATURES);

        for (String key : new String[] {
                "feature.distributionWorkflow.validation.noSelection",
                "feature.distributionWorkflow.validation.itemIdentifierMissing",
                "feature.distributionWorkflow.validation.maxItems",
                "feature.distributionWorkflow.action.createRequest"
        }) {
            assertTrue(korean.contains(key + "="), "Missing Korean key: " + key);
            assertTrue(english.contains(key + "="), "Missing English key: " + key);
        }
    }

    @Test
    void distributionMenuLabelsMatchTheRequestedKoreanAndEnglishNames()
            throws Exception {
        String[] koreanLabels = {
            "menu.technicalDataDistribution=기술자료배포",
            "menu.distributionMyRequests=배포요청",
            "menu.distributionApproval=배포승인",
            "menu.distributionApprovedList=승인목록"
        };
        String[] englishLabels = {
            "menu.technicalDataDistribution=Technical Data Distribution",
            "menu.distributionMyRequests=Distribution Requests",
            "menu.distributionApproval=Distribution Approval",
            "menu.distributionApprovedList=Approved List"
        };

        for (Path bundle : new Path[] {
                RESOURCE_DEFAULT_MESSAGES,
                RESOURCE_KOREAN_MESSAGES,
                WEBAPP_DEFAULT_MESSAGES,
                WEBAPP_KOREAN_MESSAGES,
                WEBAPP_KOREAN_KR_MESSAGES
        }) {
            String messages = read(bundle);
            for (String label : koreanLabels) {
                assertTrue(messages.contains(label),
                    "Missing Korean menu label in " + bundle + ": " + label);
            }
        }

        for (Path bundle : new Path[] {
                RESOURCE_ENGLISH_MESSAGES,
                WEBAPP_ENGLISH_MESSAGES
        }) {
            String messages = read(bundle);
            for (String label : englishLabels) {
                assertTrue(messages.contains(label),
                    "Missing English menu label in " + bundle + ": " + label);
            }
        }
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
