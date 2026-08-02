package kr.esob.tdms.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class OperationalDemoSampleSqlContractTest {

    private static final Path SAMPLE_SQL = Path.of(
            "deployment", "windows-demo", "database",
            "37-install-demo-operational-samples.sql");

    private static final Pattern MUTATION = Pattern.compile(
            "(?is)\\b(?:insert\\s+into|delete\\s+from|merge\\s+into|"
                    + "update(?!\\s+set\\b))\\s+(?:only\\s+)?"
                    + "(?:public\\.)?([a-z_][a-z0-9_]*)");
    private static final Pattern DESTRUCTIVE_MUTATION = Pattern.compile(
            "(?is)\\b(?:delete\\s+from|update(?!\\s+set\\b))\\s+"
                    + "(?:only\\s+)?(?:public\\.)?[a-z_][a-z0-9_]*.*?;");
    private static final Pattern TEMP_TABLE = Pattern.compile(
            "(?is)\\bcreate\\s+(?:temporary|temp)\\s+table\\s+"
                    + "(?:if\\s+not\\s+exists\\s+)?"
                    + "([a-z_][a-z0-9_]*)");

    private static final Set<String> OPERATIONAL_TABLES = Set.of(
            "docs_partner_company",
            "docs_partner_user",
            "docs_distribution_request",
            "docs_distribution_request_item",
            "docs_distribution_request_recipient",
            "docs_distribution_request_event",
            "docs_distribution_outbox",
            "docs_distribution_account_request",
            "docs_distribution_account_request_event");

    private static final List<String> PROTECTED_TABLES = List.of(
            "docs_sw",
            "docs_sw_file",
            "docs_sw_sub_file",
            "docs_file_security_label",
            "docs_object_user_permission",
            "docs_user_security_clearance",
            "docs_user_action_permission",
            "docs_role_group_member",
            "docs_user",
            "docs_company",
            "docs_dept",
            "docs_history",
            "docs_access_audit_log",
            "docs_print_job",
            "docs_print_job_item");

    @Test
    void scriptIsTransactionalAndCannotInvokeAResetOrSchemaDestruction()
            throws IOException {
        String sql = normalizedSql();

        assertTrue(sql.contains("\\set on_error_stop on"));
        assertEquals(1, countMatches(sql, "(?m)^\\s*begin\\s*;\\s*$"));
        assertEquals(1, countMatches(sql, "(?m)^\\s*commit\\s*;\\s*$"));
        assertTrue(sql.stripTrailing().endsWith("commit;"));

        assertFalse(sql.contains("sample_demo_data"));
        assertFalse(Pattern.compile("\\btruncate\\b").matcher(sql).find());
        assertFalse(Pattern.compile("\\bcascade\\b").matcher(sql).find());
        assertFalse(Pattern.compile("\\bdrop\\s+(?:table|schema|database)\\b")
                .matcher(sql).find());
        assertFalse(Pattern.compile("\\balter\\s+table\\b").matcher(sql).find());
    }

    @Test
    void everyPersistentMutationIsOperationalAndCleanupIsDemoScoped()
            throws IOException {
        String sql = normalizedSql();
        Matcher mutations = MUTATION.matcher(sql);
        Set<String> mutatedTables = new HashSet<>();
        Set<String> temporaryTables = temporaryTables(sql);

        while (mutations.find()) {
            String table = mutations.group(1);
            mutatedTables.add(table);
            assertTrue(OPERATIONAL_TABLES.contains(table)
                            || temporaryTables.contains(table),
                    () -> "Unexpected persistent-table mutation: " + table);
        }

        assertFalse(mutatedTables.isEmpty());
        assertTrue(mutatedTables.contains("docs_partner_company"));
        assertTrue(mutatedTables.contains("docs_partner_user"));
        assertTrue(mutatedTables.contains("docs_distribution_request"));
        assertTrue(mutatedTables.contains("docs_distribution_account_request"));

        for (String protectedTable : PROTECTED_TABLES) {
            assertFalse(mutatedTables.contains(protectedTable),
                    () -> "Protected table must remain read-only: "
                            + protectedTable);
        }

        Matcher destructive = DESTRUCTIVE_MUTATION.matcher(sql);
        while (destructive.find()) {
            String statement = destructive.group();
            assertTrue(statement.contains("demo"),
                    () -> "Cleanup/update is not DEMO-selective: " + statement);
        }
    }

    @Test
    void partnerAndWorkflowInventoryIsCompleteAndSelfValidating()
            throws IOException {
        String sql = normalizedSql();

        assertTrue(sql.contains("'demo-partner-001'"));
        assertTrue(sql.contains("'demo-partner-002'"));
        assertCountAssertion(sql, "docs_partner_company", 2);
        assertCountAssertion(sql, "docs_partner_user", 5);

        assertRecord(sql, "dreq-demo-draft-001", "draft");
        assertRecord(sql, "dreq-demo-pending-001", "pending_approval");
        assertRecord(sql, "dreq-demo-approved-001", "approved");
        assertRecord(sql, "dreq-demo-rejected-001", "rejected");
        assertCountAssertion(sql, "docs_distribution_request", 4);

        assertTrue(sql.contains("insert into docs_distribution_request_event"));
        assertTrue(sql.contains("'create'"));
        assertTrue(sql.contains("'submit'"));
        assertTrue(sql.contains("'approve'"));
        assertTrue(sql.contains("'reject'"));
        assertTrue(sql.contains("insert into docs_distribution_outbox"));
        assertTrue(sql.contains("'hold'"));
    }

    @Test
    void accountRequestsAreThreePendingReceiptsWithoutDecisionsOrNonces()
            throws IOException {
        String sql = normalizedSql();

        assertTrue(sql.contains("'demo-distribution-client'"));
        assertTrue(sql.contains("'distribution-demo'"));
        assertRecord(sql, "demo-account-register-001",
                "37000000-0000-4000-8000-000000000001",
                "register_user", "pending");
        assertRecord(sql, "demo-account-unlock-001",
                "37000000-0000-4000-8000-000000000002",
                "unlock_account", "pending");
        assertRecord(sql, "demo-account-reset-001",
                "37000000-0000-4000-8000-000000000003",
                "reset_password", "pending");
        assertCountAssertion(sql, "docs_distribution_account_request", 3);

        assertTrue(sql.contains(
                "insert into docs_distribution_account_request_event"));
        assertTrue(sql.contains("'received'"));
        assertTrue(sql.contains("decision_comment is not null"));
        assertTrue(sql.contains("decided_by_user_cd is not null"));
        assertTrue(sql.contains("decided_at is not null"));
        assertTrue(sql.contains("from docs_distribution_account_request_nonce"));
        assertTrue(sql.contains("client_id = 'demo-distribution-client'"));
        assertFalse(mutatedTables(sql).contains(
                "docs_distribution_account_request_nonce"));
    }

    @Test
    void protectedTechnicalDataIsLockedAndFingerprintedBeforeCommit()
            throws IOException {
        String sql = normalizedSql();

        assertTrue(sql.contains("lock table"));
        assertTrue(sql.contains("fingerprint"));
        assertTrue(Pattern.compile("\\b(?:md5|digest|hashtextextended)\\s*\\(")
                .matcher(sql).find());
        assertTrue(sql.contains("raise exception"));
        assertTrue(countMatches(sql, "\\braise\\s+exception\\b") >= 5);

        for (String technicalTable : List.of(
                "docs_sw", "docs_sw_file", "docs_sw_sub_file")) {
            assertTrue(sql.contains(technicalTable),
                    () -> "Missing protected-data fingerprint: "
                            + technicalTable);
        }
    }

    private String normalizedSql() throws IOException {
        assertTrue(Files.isRegularFile(SAMPLE_SQL),
                () -> "Operational sample SQL is missing: " + SAMPLE_SQL);
        String raw = Files.readString(SAMPLE_SQL, StandardCharsets.UTF_8);
        return raw
                .replaceAll("(?s)/\\*.*?\\*/", " ")
                .replaceAll("(?m)--.*$", " ")
                .toLowerCase(Locale.ROOT);
    }

    private static Set<String> mutatedTables(String sql) {
        Set<String> tables = new HashSet<>();
        Matcher matcher = MUTATION.matcher(sql);
        while (matcher.find()) {
            tables.add(matcher.group(1));
        }
        return tables;
    }

    private static Set<String> temporaryTables(String sql) {
        Set<String> tables = new HashSet<>();
        Matcher matcher = TEMP_TABLE.matcher(sql);
        while (matcher.find()) {
            tables.add(matcher.group(1));
        }
        return tables;
    }

    private static void assertRecord(String sql, String key,
            String... requiredValues) {
        int position = -1;
        while ((position = sql.indexOf("'" + key + "'", position + 1)) >= 0) {
            int start = Math.max(0, position - 600);
            int end = Math.min(sql.length(), position + 1800);
            String window = sql.substring(start, end);
            boolean complete = true;
            for (String requiredValue : requiredValues) {
                complete &= window.contains("'" + requiredValue + "'");
            }
            if (complete) {
                return;
            }
        }
        throw new AssertionError("Missing complete DEMO record for " + key
                + " with values " + List.of(requiredValues));
    }

    private static void assertCountAssertion(String sql, String table,
            int expected) {
        Pattern countThenTable = Pattern.compile(
                "(?s)count\\s*\\(\\s*\\*\\s*\\).{0,300}"
                        + Pattern.quote(table)
                        + ".{0,600}<>\\s*" + expected + "\\b");
        Pattern tableThenCount = Pattern.compile(
                "(?s)" + Pattern.quote(table)
                        + ".{0,600}count\\s*\\(\\s*\\*\\s*\\)"
                        + ".{0,300}<>\\s*" + expected + "\\b");
        assertTrue(countThenTable.matcher(sql).find()
                        || tableThenCount.matcher(sql).find(),
                () -> "Missing strong count assertion for " + table
                        + " = " + expected);
    }

    private static int countMatches(String value, String regex) {
        int count = 0;
        Matcher matcher = Pattern.compile(regex).matcher(value);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
