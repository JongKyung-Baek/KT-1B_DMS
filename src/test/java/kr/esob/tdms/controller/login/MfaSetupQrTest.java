package kr.esob.tdms.controller.login;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Test;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import static org.junit.jupiter.api.Assertions.*;

class MfaSetupQrTest {
    @Test void generatedPngDecodesToTheSameKeyAndTotpSettingsAsManualEntry() throws Exception {
        byte[] seed = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        String key = new Base32().encodeToString(seed);
        URI uri = URI.create(decode(MfaSetupQr.pngDataUrl("alice", key)));
        assertEquals("otpauth", uri.getScheme());
        assertEquals("totp", uri.getHost());
        assertEquals("/TDMS:alice", uri.getPath());
        Map<String, String> params = parameters(uri);
        assertEquals(Map.of("secret", key, "issuer", "TDMS", "algorithm", "SHA1", "digits", "6", "period", "30"), params);
        byte[] scannedSecret = new Base32().decode(params.get("secret"));
        assertArrayEquals(seed, scannedSecret);
        assertEquals("287082", MfaStore.code(scannedSecret, 59 / 30));
    }

    @Test void accountLabelCannotInjectUriParametersAndPreservesUnicode() throws Exception {
        String account = "한글 name+tag@example.com/?issuer=evil&secret=bad#part";
        URI uri = URI.create(decode(MfaSetupQr.pngDataUrl(account, "JBSWY3DP")));
        assertEquals("/TDMS:" + account, uri.getPath());
        assertNull(uri.getFragment());
        assertEquals(5, parameters(uri).size());
        assertEquals("TDMS", parameters(uri).get("issuer"));
        assertEquals("JBSWY3DP", parameters(uri).get("secret"));
        assertTrue(uri.getRawPath().contains("%20"));
    }

    @Test void qrRefreshPreservesSecretAndOmitsBase32Padding() throws Exception {
        String first = MfaSetupQr.pngDataUrl("alice", "MY======");
        assertEquals(first, MfaSetupQr.pngDataUrl("alice", "MY======"));
        assertEquals("MY", parameters(URI.create(decode(first))).get("secret"));
    }

    private static Map<String, String> parameters(URI uri) {
        return Arrays.stream(uri.getRawQuery().split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(pair -> pair[0],
                        pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8)));
    }

    private static String decode(String dataUrl) throws Exception {
        String prefix = "data:image/png;base64,";
        assertTrue(dataUrl.startsWith(prefix));
        var image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(dataUrl.substring(prefix.length()))));
        assertNotNull(image);
        int width = image.getWidth(), height = image.getHeight();
        assertEquals(280, width);
        assertEquals(280, height);
        var pixels = image.getRGB(0, 0, width, height, null, 0, width);
        var bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));
        return new QRCodeReader().decode(bitmap).getText();
    }
}
