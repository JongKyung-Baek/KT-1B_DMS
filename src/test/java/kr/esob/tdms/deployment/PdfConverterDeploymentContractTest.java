package kr.esob.tdms.deployment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PdfConverterDeploymentContractTest {

    private static final Path OVERRIDE = Path.of("deployment", "windows-demo",
            "runtime", "compose.pdf-converter.yaml");

    @Test
    void converterIsPrivateAuthenticatedAndUsesPersistentOwnedCache()
            throws IOException {
        String compose = Files.readString(OVERRIDE, StandardCharsets.UTF_8);
        String converter = compose.substring(compose.indexOf("  pdf-converter:"));

        assertTrue(compose.contains(
                "TDMS_PDF_CONVERSION_BASE_URL: http://127.0.0.1:9001"));
        assertTrue(compose.contains(
                "TDMS_PDF_CONVERSION_CLIENT_ID: ${TDMS_PDF_CONVERSION_CLIENT_ID:?"));
        assertTrue(compose.contains(
                "TDMS_PDF_CONVERSION_SHARED_SECRET: ${TDMS_PDF_CONVERSION_SHARED_SECRET:?"));
        assertTrue(converter.contains("network_mode: \"service:app\""));
        assertFalse(converter.contains("\n    ports:"));
        assertTrue(converter.contains("SERVER_ADDRESS: 127.0.0.1"));
        assertTrue(converter.contains("INTEGRATION_TDMS_ENABLED: \"true\""));
        assertTrue(converter.contains(
                "INTEGRATION_TDMS_CACHE_DIRECTORY: /var/lib/e-pdf-converter/tdms-cache"));
        assertTrue(converter.contains(
                "- pdf-converter-cache:/var/lib/e-pdf-converter/tdms-cache"));
        assertTrue(compose.contains(
                "name: kt1b-dms-pdf-converter-cache"));
        assertTrue(converter.contains("CONVERT_KEEP_TEMP_FILES: \"false\""));
        assertTrue(converter.contains("read_only: true"));
        assertTrue(converter.contains("no-new-privileges:true"));
        assertTrue(converter.contains("cap_drop:"));
    }

    @Test
    void converterImageAndCredentialsMustComeFromRuntimeEnvironment()
            throws IOException {
        String compose = Files.readString(OVERRIDE, StandardCharsets.UTF_8);

        assertTrue(compose.contains(
                "image: ${KT1B_PDF_CONVERTER_IMAGE:?"));
        assertFalse(compose.matches("(?s).*SHARED_SECRET:\\s+[A-Za-z0-9+/=_-]{32,}.*"));
        assertFalse(compose.contains("9001:9001"));
    }

    @Test
    void fileApiIsLoopbackOnlyAndPersistsIntoTheExistingStorageBackupRoot()
            throws IOException {
        String compose = Files.readString(OVERRIDE, StandardCharsets.UTF_8);
        String fileApi = compose.substring(compose.indexOf("  file-api:"),
                compose.indexOf("  pdf-converter:"));

        assertTrue(compose.contains(
                "TDMS_FILE_API_BASE_URL: http://127.0.0.1:18080"));
        assertTrue(compose.contains(
                "TDMS_FILE_API_KEY: ${KT1B_FILE_API_KEY:?"));
        assertTrue(fileApi.contains(
                "image: ${KT1B_FILE_API_IMAGE:?"));
        assertTrue(fileApi.contains("network_mode: \"service:app\""));
        assertTrue(fileApi.contains("TDDSFT_HOST: 127.0.0.1"));
        assertTrue(fileApi.contains(
                "TDDSFT_API_KEY: ${KT1B_FILE_API_KEY:?"));
        assertTrue(fileApi.contains(
                "- ../storage:/data/kt1b/files"));
        assertTrue(fileApi.contains(
                "TDDSFT_TMP_DIR: /data/kt1b/files/.tdds-ft-tmp"));
        assertFalse(fileApi.contains("\n    ports:"));
        assertTrue(fileApi.contains("read_only: true"));
        assertFalse(compose.contains("file-api-tmp:"));
    }
}
