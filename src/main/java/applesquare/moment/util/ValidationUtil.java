package applesquare.moment.util;

import applesquare.moment.auth.service.AuthService;

public class ValidationUtil {
    public static final String NICKNAME_PATTERN="^[가-힣A-Za-z0-9_-]+$";
    public static final String USERNAME_PATTERN="^[A-Za-z0-9_-]+$";
    public static final String PASSWORD_PATTERN="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!\\?@#\\$%\\^&])[A-Za-z\\d!\\?@#\\$%\\^&]+$";
}
