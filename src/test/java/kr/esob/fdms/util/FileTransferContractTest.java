package kr.esob.fdms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.sf.json.JSONObject;

class FileTransferContractTest {
    private static final String KEY_PROPERTY = "KT1B_LEGACY_CRYPTO_KEY";
    private String previousKey;

    @BeforeEach
    void configureTestOnlyKey() {
        previousKey = System.getProperty(KEY_PROPERTY);
        System.setProperty(KEY_PROPERTY, "0".repeat(16));
    }

    @AfterEach
    void restoreKeyProperty() {
        if (previousKey == null) {
            System.clearProperty(KEY_PROPERTY);
        } else {
            System.setProperty(KEY_PROPERTY, previousKey);
        }
    }

    @Test
    void transferArgumentsAreEncryptedByTheSharedContract() throws Exception {
        String ciphertext = FileUtil.encryptTransferArgument("C:\\DOCS\\sample.pdf");
        // A deterministic non-blank ciphertext is returned, not the source path.
        org.junit.jupiter.api.Assertions.assertNotEquals("C:\\DOCS\\sample.pdf", ciphertext);

        assertThrows(IllegalArgumentException.class,
            () -> FileUtil.encryptTransferArgument("  "));
        assertThrows(IllegalArgumentException.class,
            () -> FileUtil.callSender("http://plaintext/", ciphertext, ciphertext, ciphertext, ciphertext));
    }

    @Test
    void transferEndpointsMustUseHttpsBeforeAnyNetworkCall() throws Exception {
        String httpsUrl = FileUtil.encryptTransferArgument("https://files.example.test/");
        String httpUrl = FileUtil.encryptTransferArgument("http://files.example.test/");
        String encryptedValue = FileUtil.encryptTransferArgument("safe-value");

        assertThrows(IllegalArgumentException.class,
                () -> FileUtil.callSender(
                        httpUrl, httpsUrl, encryptedValue, encryptedValue, encryptedValue));
        assertThrows(IllegalArgumentException.class,
                () -> FileUtil.callSender(
                        httpsUrl, httpUrl, encryptedValue, encryptedValue, encryptedValue));
    }

    @Test
    void successfulResponseRequiresTrueAndSafeFileName() {
        JSONObject success = new JSONObject();
        success.put("result", true);
        success.put("fileNm", "1721739000_550e8400-e29b-41d4-a716-446655440000.pdf");

        assertEquals("1721739000_550e8400-e29b-41d4-a716-446655440000.pdf",
            FileUtil.requireSuccessfulTransferFileName(success));

        JSONObject failed = new JSONObject();
        failed.put("result", false);
        failed.put("fileNm", "should-not-be-used.pdf");
        assertThrows(IllegalStateException.class,
            () -> FileUtil.requireSuccessfulTransferFileName(failed));

        JSONObject traversal = new JSONObject();
        traversal.put("result", true);
        traversal.put("fileNm", "../outside.txt");
        assertThrows(IllegalStateException.class,
            () -> FileUtil.requireSuccessfulTransferFileName(traversal));
    }

    @Test
    void successfulResponseRejectsMissingOrBlankFileName() {
        JSONObject missing = new JSONObject();
        missing.put("result", true);
        assertThrows(IllegalStateException.class,
            () -> FileUtil.requireSuccessfulTransferFileName(missing));

        JSONObject blank = new JSONObject();
        blank.put("result", "true");
        blank.put("fileNm", " ");
        assertThrows(IllegalStateException.class,
            () -> FileUtil.requireSuccessfulTransferFileName(blank));
    }
}
