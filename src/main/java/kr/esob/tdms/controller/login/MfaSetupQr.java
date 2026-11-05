package kr.esob.tdms.controller.login;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/** Creates enrollment QR images entirely in memory, without an external QR service. */
final class MfaSetupQr {
    private MfaSetupQr() {}

    static String provisioningUri(String account, String setupKey) {
        return "otpauth://totp/TDMS:" + encode(account)
                + "?secret=" + encode(setupKey.replace("=", ""))
                + "&issuer=TDMS&algorithm=SHA1&digits=6&period=30";
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    static String pngDataUrl(String account, String setupKey) {
        try {
            var matrix = new QRCodeWriter().encode(provisioningUri(account, setupKey),
                    BarcodeFormat.QR_CODE, 280, 280,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                            EncodeHintType.MARGIN, 4));
            var image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            for (int y = 0; y < matrix.getHeight(); y++) {
                for (int x = 0; x < matrix.getWidth(); x++) {
                    image.setRGB(x, y, matrix.get(x, y) ? 0xff000000 : 0xffffffff);
                }
            }
            var bytes = new ByteArrayOutputStream();
            // Explicit memory-only stream: ImageIO must not cache the enrollment secret on disk.
            try (var output = new MemoryCacheImageOutputStream(bytes)) {
                if (!ImageIO.write(image, "PNG", output)) throw new IOException("PNG encoder unavailable");
            }
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (WriterException | IOException e) {
            // Do not include the provisioning URI or secret in an error message.
            throw new IllegalStateException("Cannot generate OTP enrollment QR code");
        }
    }
}
