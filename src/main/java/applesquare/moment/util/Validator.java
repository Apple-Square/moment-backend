package applesquare.moment.util;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import java.util.Set;

public class Validator {
    public static final String NICKNAME_PATTERN="^(?! )[가-힣A-Za-z0-9_-]+( [가-힣A-Za-z0-9_-]+)*(?<! )$";
    public static final String USERNAME_PATTERN="^[A-Za-z0-9_-]+$";
    public static final String PASSWORD_PATTERN="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!\\?@#\\$%\\^&])[A-Za-z\\d!\\?@#\\$%\\^&]+$";

    private static final jakarta.validation.Validator validator=Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> void validate(T object){
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            // 검증 오류가 있을 경우
            throw new ConstraintViolationException(violations);
        }
    }
}
