package kr.esob.tdms.util.seed;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtils {

    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final int SHORT_ALPHANUMERIC_COUNT = 8;
    private static final int SHORT_SPECIAL_COUNT = 3;
    private static final int LONG_ALPHANUMERIC_COUNT = 10;
    private static final int LONG_SPECIAL_COUNT = 2;
    private static final String PBKDF2_VERSION = "pbkdf2-sha256";
    private static final String PBKDF2_PREFIX = PBKDF2_VERSION + "$";
    private static final int PBKDF2_ITERATIONS = 310_000;
    private static final int MIN_SUPPORTED_ITERATIONS = 100_000;
    private static final int MAX_SUPPORTED_ITERATIONS = 999_999;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private PasswordUtils() {
    }

    public static String getSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String getSHA256Hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedPassword = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not found", e);
        }
    }

    public static String hashPasswordWithSalt(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }

        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        char[] passwordChars = password.toCharArray();
        byte[] hash = null;
        try {
            hash = derivePbkdf2(passwordChars, salt, PBKDF2_ITERATIONS);
            return PBKDF2_VERSION
                    + "$" + PBKDF2_ITERATIONS
                    + "$" + URL_ENCODER.encodeToString(salt)
                    + "$" + URL_ENCODER.encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA256 is not available", e);
        } finally {
            Arrays.fill(passwordChars, '\0');
            if (hash != null) {
                Arrays.fill(hash, (byte) 0);
            }
        }
    }

    public static boolean isAcceptablePassword(String password) {
        if (password == null || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }

        int letterCount = 0;
        int digitCount = 0;
        int alphanumericCount = 0;
        int specialCount = 0;
        for (int i = 0; i < password.length(); i++) {
            char character = password.charAt(i);
            // Keep the browser and server rules deterministic: printable ASCII
            // only, with no whitespace or control characters.
            if (character < 33 || character > 126) {
                return false;
            }
            if ((character >= 'A' && character <= 'Z')
                    || (character >= 'a' && character <= 'z')) {
                letterCount++;
                alphanumericCount++;
            } else if (character >= '0' && character <= '9') {
                digitCount++;
                alphanumericCount++;
            } else {
                specialCount++;
            }
        }

        if (letterCount == 0 || digitCount == 0) {
            return false;
        }

        boolean shortRule = alphanumericCount >= SHORT_ALPHANUMERIC_COUNT
                && specialCount >= SHORT_SPECIAL_COUNT;
        boolean longRule = alphanumericCount >= LONG_ALPHANUMERIC_COUNT
                && specialCount >= LONG_SPECIAL_COUNT;
        return shortRule || longRule;
    }

    public static boolean verifyPassword(String storedPasswordWithSalt, String originalPassword) {
        if (storedPasswordWithSalt == null || originalPassword == null) {
            return false;
        }

        if (storedPasswordWithSalt.startsWith(PBKDF2_PREFIX)) {
            return verifyPbkdf2(storedPasswordWithSalt, originalPassword);
        }
        return verifyLegacySha256(storedPasswordWithSalt, originalPassword);
    }

    private static boolean verifyPbkdf2(String storedPassword, String originalPassword) {
        String[] parts = storedPassword.split("\\$", -1);
        if (parts.length != 4 || !PBKDF2_VERSION.equals(parts[0])) {
            return false;
        }

        byte[] salt;
        byte[] storedHash;
        int iterations;
        try {
            if (parts[1].length() != 6 || parts[1].charAt(0) == '0') {
                return false;
            }
            iterations = Integer.parseInt(parts[1]);
            if (iterations < MIN_SUPPORTED_ITERATIONS || iterations > MAX_SUPPORTED_ITERATIONS) {
                return false;
            }

            salt = URL_DECODER.decode(parts[2]);
            storedHash = URL_DECODER.decode(parts[3]);
            if (salt.length != SALT_BYTES || storedHash.length != HASH_BYTES
                    || !parts[2].equals(URL_ENCODER.encodeToString(salt))
                    || !parts[3].equals(URL_ENCODER.encodeToString(storedHash))) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }

        char[] passwordChars = originalPassword.toCharArray();
        byte[] candidateHash = null;
        try {
            candidateHash = derivePbkdf2(passwordChars, salt, iterations);
            return MessageDigest.isEqual(candidateHash, storedHash);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        } finally {
            Arrays.fill(passwordChars, '\0');
            Arrays.fill(salt, (byte) 0);
            Arrays.fill(storedHash, (byte) 0);
            if (candidateHash != null) {
                Arrays.fill(candidateHash, (byte) 0);
            }
        }
    }

    private static boolean verifyLegacySha256(String storedPasswordWithSalt, String originalPassword) {
        int separator = storedPasswordWithSalt.indexOf(':');
        if (separator <= 0
                || separator == storedPasswordWithSalt.length() - 1
                || storedPasswordWithSalt.indexOf(':', separator + 1) >= 0) {
            return false;
        }

        String salt = storedPasswordWithSalt.substring(0, separator);
        String storedHash = storedPasswordWithSalt.substring(separator + 1);
        try {
            byte[] decodedSalt = Base64.getDecoder().decode(salt);
            byte[] decodedStoredHash = Base64.getDecoder().decode(storedHash);
            if (decodedSalt.length != SALT_BYTES || decodedStoredHash.length != HASH_BYTES) {
                return false;
            }
            byte[] decodedCandidateHash =
                    Base64.getDecoder().decode(getSHA256Hash(originalPassword, salt));
            return MessageDigest.isEqual(decodedCandidateHash, decodedStoredHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivePbkdf2(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BYTES * 8);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}
