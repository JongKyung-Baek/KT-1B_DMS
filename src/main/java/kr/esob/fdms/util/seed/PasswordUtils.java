package kr.esob.fdms.util.seed;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class PasswordUtils{

    // 솔트 생성
    public static String getSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // SHA-256 해싱 함수
    public static String getSHA256Hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8)); // 솔트 추가
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // 비밀번호와 솔트를 함께 해싱한 값을 반환
    public static String hashPasswordWithSalt(String password) {
        String salt = getSalt();
        String hashedPassword = getSHA256Hash(password, salt);
        // 해싱된 비밀번호와 솔트를 함께 저장
        return salt + ":" + hashedPassword;
    }

    // 저장된 해시와 사용자가 입력한 비밀번호를 비교하는 메서드
    public static boolean verifyPassword(String storedPasswordWithSalt, String originalPassword) {
        try {
            String[] parts = storedPasswordWithSalt.split(":");

            String salt = parts[0];
            String storedHash = parts[1];

            String newHash = getSHA256Hash(originalPassword, salt);
            return newHash.equals(storedHash);
        }catch (Exception e){
            log.error("Error on: ", e);
            return false;
        }
    }

}
