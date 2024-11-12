package applesquare.moment.email.service;

public interface EmailValidationService {
    String EMAIL_STATE_COOKIE="email-state";
    int EMAIL_STATE_COOKIE_MAX_AGE=60*10;
    int EMAIL_STATE_TTL_MINUTE=10;
    int EMAIL_CODE_LEN=6;
    int EMAIL_CODE_TTL_MINUTE=10;


    void storeAndSendEmailCode(String email);
    void validateEmailCode(String email, String code);
    void removeEmailCode(String email);
}
