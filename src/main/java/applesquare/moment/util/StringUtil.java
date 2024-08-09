package applesquare.moment.util;

import java.security.SecureRandom;

public class StringUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALLOWED_CHARACTER = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 주어진 길이의 랜덤 문자열 생성
     * @param length 문자열 길이
     * @return 랜덤 문자열
     */
    public static String generateRandomString(int length) {
        int charLength=ALLOWED_CHARACTER.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex=RANDOM.nextInt(charLength);
            sb.append(ALLOWED_CHARACTER.charAt(randomIndex));
        }
        return sb.toString();
    }
}
