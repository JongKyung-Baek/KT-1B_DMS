package kr.esob.tdms.controller.login;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MfaStore {
    private final JdbcTemplate db;
    private final String masterKey;
    private final SecureRandom random = new SecureRandom();

    public MfaStore(JdbcTemplate db, @Value("${TDMS_MFA_KEY:}") String masterKey) {
        this.db = db;
        this.masterKey = masterKey;
    }

    record Enrollment(long generation, byte[] setupKey) {}

    Enrollment enrollment(String userCd) {
        key(); // Fail before displaying a setup key unless it can be stored securely.
        db.update("INSERT INTO docs_user_totp(user_cd) VALUES (?) ON CONFLICT DO NOTHING", userCd);
        return db.queryForObject("SELECT generation, encrypted_secret IS NOT NULL FROM docs_user_totp WHERE user_cd=?",
                (rs,n) -> new Enrollment(rs.getLong(1), rs.getBoolean(2) ? null : random.generateSeed(20)), userCd);
    }

    public boolean reset(String userCd) {
        return db.update("UPDATE docs_user_totp SET encrypted_secret=NULL, last_step=-1, failures=0, "
                + "blocked_until=NULL, generation=generation+1 WHERE user_cd=?", userCd) == 1;
    }

    private SecretKeySpec key() {
        byte[] bytes = Base64.getDecoder().decode(masterKey);
        if (bytes.length != 32) throw new IllegalStateException("Configure TDMS_MFA_KEY with a Base64-encoded 256-bit key");
        return new SecretKeySpec(bytes, "AES");
    }

    private byte[] crypt(int mode, String userCd, byte[] input) throws GeneralSecurityException {
        boolean encrypt = mode == Cipher.ENCRYPT_MODE;
        byte[] iv = encrypt ? random.generateSeed(12) : Arrays.copyOf(input, 12);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, key(), new GCMParameterSpec(128, iv));
        cipher.updateAAD(userCd.getBytes(StandardCharsets.UTF_8));
        if (!encrypt) return cipher.doFinal(input, 12, input.length - 12);
        byte[] encrypted = cipher.doFinal(input);
        return ByteBuffer.allocate(12 + encrypted.length).put(iv).put(encrypted).array();
    }

    static String code(byte[] secret, long step) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret, "HmacSHA1"));
        byte[] digest = mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());
        int offset = digest[digest.length - 1] & 15;
        int value = ByteBuffer.wrap(digest, offset, 4).getInt() & Integer.MAX_VALUE;
        return String.format(Locale.ROOT, "%06d", value % 1_000_000);
    }

    private record Factor(byte[] encrypted, long lastStep, boolean blocked) {}

    @Transactional
    public boolean verify(MfaStart.Challenge pending, String otp) throws GeneralSecurityException {
        String id = pending.userCd();
        List<Factor> rows = db.query("SELECT encrypted_secret,last_step,blocked_until > now() FROM docs_user_totp WHERE user_cd=? AND generation=? FOR UPDATE",
                (rs,n) -> new Factor(rs.getBytes(1),rs.getLong(2),rs.getBoolean(3)), id, pending.generation());
        if (rows.isEmpty() || rows.get(0).blocked() || System.currentTimeMillis() >= pending.expires()) return false;
        Factor factor = rows.get(0);
        boolean enrolling = factor.encrypted() == null;
        if (enrolling != (pending.enrollment() != null)) return false;
        byte[] secret = enrolling ? pending.enrollment() : crypt(Cipher.DECRYPT_MODE, id, factor.encrypted());
        long nowStep = Instant.now().getEpochSecond() / 30;
        if (otp != null && otp.matches("[0-9]{6}")) {
            for (long step = nowStep - 1; step <= nowStep + 1; step++) {
                if (step > factor.lastStep() && MessageDigest.isEqual(code(secret, step).getBytes(StandardCharsets.US_ASCII),
                        otp.getBytes(StandardCharsets.US_ASCII))) {
                    db.update("UPDATE docs_user_totp SET encrypted_secret=?,last_step=?,failures=0,blocked_until=NULL WHERE user_cd=?",
                            enrolling ? crypt(Cipher.ENCRYPT_MODE, id, secret) : factor.encrypted(), step, id);
                    return true;
                }
            }
        }
        db.update("UPDATE docs_user_totp SET failures=(failures+1)%5, blocked_until=CASE WHEN failures>=4 THEN now()+interval '5 minutes' END WHERE user_cd=?", id);
        return false;
    }
}
