package applesquare.moment.util;

import java.security.SecureRandom;

public class StringUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final String USER_ID_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String NUMBERS="0123456789";
    /**
     * 주어진 길이의 랜덤 문자열 생성
     * @param length 문자열 길이
     * @return 랜덤 문자열
     */
    public static String generateRandomString(String allowedCharacters, int length) {
        int charLength=allowedCharacters.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex=RANDOM.nextInt(charLength);
            sb.append(allowedCharacters.charAt(randomIndex));
        }
        return sb.toString();
    }
}
