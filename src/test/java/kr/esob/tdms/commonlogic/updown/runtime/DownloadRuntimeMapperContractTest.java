package kr.esob.tdms.commonlogic.updown.runtime;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class DownloadRuntimeMapperContractTest {

    @Test
    void ddlContainsTheCompleteDurableCapabilityLifecycle() throws Exception {
        String ddl = read("src/main/resources/sql/download_runtime_ddl.sql");

        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS docs_download_runtime"));
        assertTrue(ddl.contains("status_cd"));
        assertTrue(ddl.contains("owner_user_cd"));
        assertTrue(ddl.contains("owner_session_id"));
        assertTrue(ddl.contains("temp_file_path"));
        assertTrue(ddl.contains("download_claimed"));
        assertTrue(ddl.contains("claimed_at"));
        assertTrue(ddl.contains("audit_saved"));
        assertTrue(ddl.contains("expire_at"));
        assertTrue(ddl.contains("'QUEUED'"));
        assertTrue(ddl.contains("'DOWNLOADING'"));
        assertTrue(ddl.contains("'SENT_TO_WS'"));
        assertTrue(ddl.contains("'COMPLETED'"));
        assertTrue(ddl.contains("'FAILED'"));
        assertTrue(ddl.contains("download_request_key  varchar(32) NOT NULL UNIQUE"));
    }

    @Test
    void mapperUsesAnAtomicSingleUseClaimAndDurableRecoveryQueries()
            throws Exception {
        String mapper = read(
            "src/main/resources/sqlMaps/oracle/its/commonlogic/updown/DownloadRuntime.xml");

        assertTrue(mapper.contains("<insert id=\"insertQueued\""));
        assertTrue(mapper.contains("<update id=\"updateState\""));
        assertTrue(mapper.contains("<select id=\"claimByDownloadRequestKey\""));
        assertTrue(mapper.contains("WITH candidate AS"));
        assertTrue(mapper.contains("download_claimed = FALSE"));
        assertTrue(mapper.contains("status_cd = 'SENT_TO_WS'"));
        assertTrue(mapper.contains("expire_at &gt; CURRENT_TIMESTAMP"));
        assertTrue(mapper.contains("FOR UPDATE SKIP LOCKED"));
        assertTrue(mapper.contains("RETURNING runtime.*"));
        assertTrue(mapper.contains(
            "<select id=\"selectRestartRecoveryCandidates\""));
        assertTrue(mapper.contains("<update id=\"markRestartRecoveryFailed\""));
        assertTrue(mapper.contains("<update id=\"markAuditSaved\""));
        assertTrue(mapper.contains("audit_saved = FALSE"));

        int updateStart = mapper.indexOf("<update id=\"updateState\"");
        int updateEnd = mapper.indexOf("</update>", updateStart);
        String updateSql = mapper.substring(updateStart, updateEnd);
        assertTrue(!updateSql.contains("download_claimed ="));
        assertTrue(!updateSql.contains("claimed_at ="));
    }

    @Test
    void tempPathIsPersistedBeforeAnyFileWriteStarts() throws Exception {
        String service = read(
            "src/main/java/kr/esob/tdms/commonlogic/updown/CommonUpdownV2Service.java");

        int restRegister = service.indexOf(
            "tempPathRegistrar.register(target.getAbsolutePath(), storedFileName)");
        int restWrite = service.indexOf("new FileOutputStream(target)", restRegister);
        int localRegister = service.indexOf(
            "tempPathRegistrar.register(target.getAbsolutePath(), storedFileName)",
            restRegister + 1);
        int localCopy = service.indexOf("Files.copy(", localRegister);

        assertTrue(restRegister >= 0 && restWrite > restRegister);
        assertTrue(localRegister > restRegister && localCopy > localRegister);
    }

    private String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)),
            StandardCharsets.UTF_8);
    }
}
