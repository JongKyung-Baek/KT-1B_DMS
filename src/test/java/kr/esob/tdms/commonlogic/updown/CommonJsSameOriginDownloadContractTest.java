package kr.esob.tdms.commonlogic.updown;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class CommonJsSameOriginDownloadContractTest {

    @Test
    void activeDownloadFlowUsesOnlyTheServerIssuedSameOriginCapability()
            throws Exception {
        String source = new String(Files.readAllBytes(Paths.get(
            "src/main/resources/static/js/common.js")), StandardCharsets.UTF_8);
        int activeFlowStart = source.lastIndexOf("function callDownload(response)");
        assertTrue(activeFlowStart >= 0);
        String activeFlow = source.substring(activeFlowStart);

        assertTrue(source.contains(
            "link.href = window.location.origin + downloadContextPath"));
        assertTrue(source.contains(
            "+ \"/download/\" + encodeURIComponent(downloadTicket);"));
        assertTrue(source.contains(
            "typeof CONTEXT_PATH === \"string\" ? CONTEXT_PATH.replace"));
        assertTrue(activeFlow.contains(
            "var downloadTicket = startRes && startRes.downloadRequestKey"));
        assertTrue(activeFlow.contains("issueSameOriginDownload(downloadTicket);"));
        assertTrue(activeFlow.contains("pollDownloadStatus(wsSeq"));
        assertFalse(activeFlow.contains("new WebSocket"));
        assertFalse(activeFlow.contains("buildLegacyDownloadPacket"));
        assertFalse(source.contains("ws://localhost:39229"));
        assertFalse(source.contains("localhost:39229"));
        assertFalse(source.contains("new WebSocket"));
        assertFalse(source.contains("callDownloadViaLegacyWebSocket"));
    }
}
