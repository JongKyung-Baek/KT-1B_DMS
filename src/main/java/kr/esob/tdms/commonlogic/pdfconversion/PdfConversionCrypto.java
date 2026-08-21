package kr.esob.tdms.commonlogic.pdfconversion;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

final class PdfConversionCrypto {
    private PdfConversionCrypto() {
    }

    static String sha256(Path path) {
        try (InputStream input = Files.newInputStream(path)) {
            return sha256(input);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash the conversion file.", exception);
        }
    }

    static String sha256(InputStream input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return hex(digest.digest());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash the conversion stream.", exception);
        }
    }

    static String hmacSha256(String secret, String canonical) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return hex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign the PDF conversion request.", exception);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder value = new StringBuilder(bytes.length * 2);
        for (byte item : bytes) {
            value.append(String.format("%02x", item & 0xff));
        }
        return value.toString();
    }
}
