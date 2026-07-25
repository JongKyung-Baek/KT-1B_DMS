package kr.esob.fdms.util.seed;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilsTest {

    @Test
    void createsAndVerifiesVersionedPbkdf2Password() {
        String storedPassword = PasswordUtils.hashPasswordWithSalt("Example9!");

        assertTrue(storedPassword.matches(
                "^pbkdf2-sha256\\$310000\\$[A-Za-z0-9_-]{21}[AQgw]"
                        + "\\$[A-Za-z0-9_-]{42}[AEIMQUYcgkosw048]$"));
        assertTrue(PasswordUtils.verifyPassword(storedPassword, "Example9!"));
        assertFalse(PasswordUtils.verifyPassword(storedPassword, "Different9!"));
        assertNotEquals(storedPassword, PasswordUtils.hashPasswordWithSalt("Example9!"));
    }

    @Test
    void continuesToVerifyLegacySaltedSha256Password() {
        String salt = PasswordUtils.getSalt();
        String legacyPassword = salt + ":" + PasswordUtils.getSHA256Hash("Example9!", salt);

        assertTrue(PasswordUtils.verifyPassword(legacyPassword, "Example9!"));
        assertFalse(PasswordUtils.verifyPassword(legacyPassword, "Different9!"));
    }

    @Test
    void malformedValuesFailClosedWithoutThrowing() {
        assertFalse(PasswordUtils.verifyPassword(null, "Example9!"));
        assertFalse(PasswordUtils.verifyPassword("", "Example9!"));
        assertFalse(PasswordUtils.verifyPassword("missing-separator", "Example9!"));
        assertFalse(PasswordUtils.verifyPassword(":missing-salt", "Example9!"));
        assertFalse(PasswordUtils.verifyPassword("missing-hash:", "Example9!"));
        assertFalse(PasswordUtils.verifyPassword("too:many:parts", "Example9!"));
        assertFalse(PasswordUtils.verifyPassword("salt:hash", null));
        assertFalse(PasswordUtils.verifyPassword(
                "pbkdf2-sha256$99999$AAAAAAAAAAAAAAAAAAAAAA$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "Example9!"));
        assertFalse(PasswordUtils.verifyPassword(
                "pbkdf2-sha256$010000$AAAAAAAAAAAAAAAAAAAAAA$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "Example9!"));
        assertFalse(PasswordUtils.verifyPassword(
                "pbkdf2-sha256$310000$bad!salt$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "Example9!"));
        assertFalse(PasswordUtils.verifyPassword(
                "pbkdf2-sha256$310000$AAAAAAAAAAAAAAAAAAAAAA$short",
                "Example9!"));
        assertFalse(PasswordUtils.verifyPassword(
                "pbkdf2-sha256$310000$AAAAAAAAAAAAAAAAAAAAAA$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA$extra",
                "Example9!"));
    }

    @Test
    void appliesSharedServerSidePasswordPolicy() {
        assertTrue(PasswordUtils.isAcceptablePassword("Example9!"));
        assertFalse(PasswordUtils.isAcceptablePassword("short"));
        assertFalse(PasswordUtils.isAcceptablePassword("contains whitespace9!"));
        assertFalse(PasswordUtils.isAcceptablePassword("123456789012345678901"));
    }
}
