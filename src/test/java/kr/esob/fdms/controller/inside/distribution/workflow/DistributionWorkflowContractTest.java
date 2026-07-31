package kr.esob.fdms.controller.inside.distribution.workflow;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        "src/main/resources/sqlMaps/oracle/its/controller/inside/distribution/workflow/DistributionWorkflow.xml");
    private static final Path DDL = ROOT.resolve(
        "src/main/resources/sql/distribution_workflow_ddl.sql");
    private static final Path SERVICE = ROOT.resolve(
        "src/main/java/kr/esob/fdms/controller/inside/distribution/workflow/DistributionWorkflowService.java");
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
        assertFalse(mapper.toLowerCase().contains("file_path_nm"));
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
        assertTrue(configuration.hasStatement("sql.DistributionWorkflow.insertOutboxHold"));
    }

    @Test
    void migrationIsRepeatableAndDefinesTheCompleteLifecycle() throws Exception {
        String ddl = read(DDL);
        String freshMigration = read(FRESH_MIGRATION);

        assertTrue(ddl.contains("CREATE SEQUENCE IF NOT EXISTS"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_request"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_request_item"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_request_event"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_distribution_outbox"));
        assertTrue(ddl.contains("CREATE OR REPLACE VIEW docs_approved_distribution_list"));
        for (DistributionWorkflowStatus status : DistributionWorkflowStatus.values()) {
            assertTrue(ddl.contains("'" + status.name() + "'"));
        }
        assertTrue(ddl.contains("UNIQUE (request_id)"));
        assertTrue(ddl.contains("status IN ('HOLD', 'READY', 'SENDING', 'SENT', 'FAILED', 'DEAD')"));
        assertFalse(ddl.toLowerCase().contains("file_path"));
        assertTrue(freshMigration.contains("\\ir distribution_workflow_ddl.sql"));
        assertTrue(freshMigration.indexOf("\\ir acl_foundation_ddl.sql")
            < freshMigration.indexOf("\\ir distribution_workflow_ddl.sql"));
    }

    @Test
    void clientDtosHaveIdentifiersButNoActorMetadataOrFilePath() {
        Set<String> itemFields = Arrays.stream(DistributionRequestItemRef.class.getDeclaredFields())
            .map(Field::getName).collect(Collectors.toSet());
        Set<String> requestFields = Arrays.stream(DistributionRequestSaveRequest.class.getDeclaredFields())
            .map(Field::getName).collect(Collectors.toSet());

        assertTrue(itemFields.containsAll(Arrays.asList("objectType", "objectId", "fileNo")));
        assertFalse(itemFields.stream().anyMatch(name -> name.toLowerCase().contains("path")));
        assertFalse(itemFields.stream().anyMatch(name -> name.toLowerCase().contains("user")));
        assertFalse(requestFields.stream().anyMatch(name -> name.toLowerCase().contains("path")));
        assertFalse(requestFields.stream().anyMatch(name -> name.toLowerCase().contains("user")));
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
            "src/main/java/kr/esob/fdms/controller/inside/distribution/workflow");
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
