package com.renee328.admin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 비밀번호를 BCrypt 알고리즘으로 암호화합니다.
     * @param rawPassword 원본 비밀번호
     * @return 암호화된 비밀번호 (해시값)
     */
    public static String encryptPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 입력한 비밀번호가 암호화된 비밀번호와 일치하는지 검증합니다.
     * @param rawPassword 입력한 비밀번호
     * @param encryptedPassword 저장된 암호화된 비밀번호
     * @return 일치하면 true, 그렇지 않으면 false
     */
    public static boolean verifyPassword(String rawPassword, String encryptedPassword) {
        return encoder.matches(rawPassword, encryptedPassword);
    }

    public static void main(String[] args) {
        String rawPassword = "1234qwer!";
        String hashedPassword = encryptPassword(rawPassword);

        System.out.println("암호화된 비밀번호: " + hashedPassword);
        System.out.println("비밀번호 검증 결과: " + verifyPassword(rawPassword, hashedPassword));
    }
}
