package kr.esob.tdms.controller.general.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.springframework.transaction.annotation.Transactional;

class DistributionWorkflowContractTest {
    private static final Path ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path MAPPER = ROOT.resolve(
        "src/main/resources/sqlMaps/oracle/its/controller/general/distribution/workflow/DistributionWorkflow.xml");
    private static final Path DDL = ROOT.resolve(
        "src/main/resources/sql/distribution_workflow_ddl.sql");
    private static final Path SERVICE = ROOT.resolve(
        "src/main/java/kr/esob/tdms/controller/general/distribution/workflow/DistributionWorkflowService.java");
    private static final Path FRESH_MIGRATION = ROOT.resolve(
        "src/main/resources/sql/fresh_database_migration.psql");

    @Test
    void mapperLocksTransitionsAndCreatesIdempotentHoldSnapshotWithoutPaths() throws Exception {
        String mapper = read(MAPPER);

        assertTrue(mapper.contains("FOR UPDATE OF request_row"));
        assertTrue(mapper.contains("status = #{expectedStatus}"));
        assertTrue(mapper.contains("'PENDING_APPROVAL'"));
        assertTrue(mapper.contains("'APPROVED'"));
        assertTrue(mapper.contains("INSERT INTO docs_distribution_outbox"));
        assertTrue(mapper.contains("'HOLD'"));
        assertTrue(mapper.contains("ON CONFLICT (request_id) DO NOTHING"));
        assertTrue(mapper.contains("request_row.status = 'APPROVED'"));
        assertTrue(mapper.contains("COALESCE(file_info.org_file_nm, file_info.file_nm, '')"));
        assertTrue(mapper.contains("9223372036854775807::numeric"));
        assertFalse(mapper.toLowerCase().contains("file_path_nm"));
        assertTrue(mapper.contains("CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul'"));
        assertFalse(mapper.contains("CURRENT_DATE"));
        assertTrue(mapper.contains("WITH actor_context AS"));
        assertTrue(mapper.contains("BOOL_AND("));
        assertFalse(mapper.contains("LIMIT 500"));
        assertFalse(mapper.contains("allViewable"));
        assertTrue(mapper.contains("BTRIM(main_file.processing_status)"));
        assertTrue(mapper.contains("BTRIM(sub_file.processing_status)"));
        assertEquals(4, mapper.split("IN \\('DONE', 'NOT_VIEWABLE'\\)", -1).length - 1);
        assertTrue(mapper.contains("status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED')"));
        assertTrue(mapper.contains("'parentTreeName', item.parent_tree_nm"));
        String service = read(SERVICE);
        assertFalse(service.contains("RestTemplate"));
        assertFalse(service.contains("HttpClient"));
        assertFalse(service.contains("WebClient"));
        assertFalse(mapper.contains("commonrequest"));
    }

    @Test
    void mapperIsParseableByMyBatis() throws Exception {
        Configuration configuration = new Configuration();
        try (InputStream input = Files.newInputStream(MAPPER)) {
            XMLMapperBuilder builder = new XMLMapperBuilder(
                input, configuration, MAPPER.toString(), configuration.getSqlFragments());
            builder.parse();
        }
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.selectRequest"));
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.selectEvents"));
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.selectAccessibleApprovedRequests"));
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.selectAccessibleCatalogItems"));
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.resolveDocumentFiles"));
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.selectRecipients"));
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.selectApprovers"));
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.expireElapsedRequests"));
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.insertOutboxHold"));
    }

    @Test
    void migrationIsRepeatableAndDefinesTheCompleteLifecycle() throws Exception {
        String ddl = read(DDL);
        String freshMigration = read(FRESH_MIGRATION);

        assertTrue(ddl.contains("CREATE SEQUENCE IF NOT EXISTS"));
        assertTrue(ddl.contains("\\set ON_ERROR_STOP on"));
        assertTrue(ddl.contains("BEGIN;"));
        assertTrue(ddl.contains("COMMIT;"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_request"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_request_item"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_request_recipient"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_request_event"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_outbox"));
        assertTrue(ddl.contains("DROP VIEW IF EXISTS docs_approved_distribution_list"));
        assertTrue(ddl.contains("CREATE VIEW docs_approved_distribution_list"));
        for (DistributionWorkflowStatus status : DistributionWorkflowStatus.values()) {
            assertTrue(ddl.contains("'" + status.name() + "'"));
        }
        assertTrue(ddl.contains("UNIQUE (request_id)"));
        assertTrue(ddl.contains("distribution_end_date >= (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date"));
        assertTrue(ddl.contains("distribution_start_date <= (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Seoul')::date"));
        assertFalse(ddl.contains("CURRENT_DATE"));
        assertTrue(ddl.contains("document_line_no"));
        assertTrue(ddl.contains("parent_tree_nm"));
        assertTrue(ddl.contains("status IN ('HOLD', 'READY', 'SENDING', 'SENT', 'FAILED', 'DEAD')"));
        assertFalse(ddl.toLowerCase().contains("file_path"));
        assertTrue(freshMigration.contains("\\ir distribution_workflow_ddl.sql"));
        assertTrue(freshMigration.indexOf("\\ir acl_foundation_ddl.sql")
            < freshMigration.indexOf("\\ir distribution_workflow_ddl.sql"));
    }

    @Test
    void migrationBackfillsLegacyRequestsBeforeEnforcingCurrentConstraints()
            throws Exception {
        String ddl = read(DDL);
        int placeholder = ddl.indexOf("'TDMS-LEGACY-UNASSIGNED'");
        int backfillValidation = ddl.indexOf(
            "$validate_distribution_request_backfill$");
        int notNullConstraint = ddl.indexOf(
            "ALTER COLUMN partner_company_id SET NOT NULL");

        assertTrue(placeholder >= 0);
        assertTrue(backfillValidation > placeholder);
        assertTrue(notNullConstraint > backfillValidation);
        assertTrue(ddl.contains("SET status = CASE"));
        assertTrue(ddl.contains("ELSE 'CANCELLED'"));
        assertTrue(ddl.contains(
            "requester.user_cd = request_row.requested_by_user_cd"));
        assertTrue(ddl.contains("administrator.group_code = 'RG_001'"));
        assertTrue(ddl.contains(
            "Cannot backfill legacy distribution approver: docs_user is empty."));
        assertTrue(ddl.contains(
            "ALTER COLUMN distribution_start_date SET NOT NULL"));
        assertTrue(ddl.contains(
            "ALTER COLUMN distribution_end_date SET NOT NULL"));
        assertFalse(ddl.contains("expected to clear those drafts"));
    }

    @Test
    void clientDtosHaveIdentifiersButNoActorMetadataOrFilePath() {
        Set<String> documentFields = Arrays.stream(DistributionRequestDocumentRef.class.getDeclaredFields())
            .map(Field::getName).collect(Collectors.toSet());
        Set<String> requestFields = Arrays.stream(DistributionRequestSaveRequest.class.getDeclaredFields())
            .map(Field::getName).collect(Collectors.toSet());

        assertEquals(java.util.Collections.singleton("objectId"), documentFields);
        assertFalse(requestFields.stream().anyMatch(name -> name.toLowerCase().contains("path")));
        assertTrue(requestFields.containsAll(Arrays.asList(
            "partnerCompanyId", "recipientUserIds", "approverUserCd",
            "distributionStartDate", "distributionEndDate", "documents")));
    }

    @Test
    void everyMutationBoundaryIsTransactional() throws Exception {
        for (String method : Arrays.asList("create", "update", "submit", "approve", "reject", "cancel")) {
            boolean found = Arrays.stream(DistributionWorkflowService.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(method))
                .anyMatch(candidate -> candidate.isAnnotationPresent(Transactional.class));
            assertTrue(found, method + " must remain transactional");
        }
    }

    @Test
    void newWorkflowDoesNotReuseLegacyCommonRequestPackage() throws Exception {
        Path javaRoot = ROOT.resolve(
            "src/main/java/kr/esob/tdms/controller/general/distribution/workflow");
        try (java.util.stream.Stream<Path> files = Files.walk(javaRoot)) {
            String combined = files.filter(path -> path.toString().endsWith(".java"))
                .map(path -> {
                    try {
                        return read(path);
                    } catch (Exception exception) {
                        throw new IllegalStateException(exception);
                    }
                }).collect(Collectors.joining("\n"));
            assertFalse(combined.contains("distribution.commonrequest"));
        }
    }

    private String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
