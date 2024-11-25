package applesquare.moment.auth.service;

import applesquare.moment.auth.dto.UserCreateRequestDTO;

public interface AuthService {
    int MIN_USERNAME_LENGTH=8;
    int MAX_USERNAME_LENGTH=20;
    int MIN_PASSWORD_LENGTH=10;
    int MAX_PASSWORD_LENGTH=20;

    int PW_RESET_TOKEN_TTL_MINUTE=10;
    String PW_RESET_URL="http://moment.com/reset-password";

    // 새로운 유저 생성 (회원가입)
    void createUser(UserCreateRequestDTO userCreateRequestDTO, String emailState);
    // 계정 복구 메일 전송 (ID/PW 찾기)
    void sendAccountRecoveryEmail(String email);
    // 비밀번호 재설정
    void resetPassword(String username, String newPassword);

    // 중복되지 않는 username인지 검사
    boolean isUniqueUsername(String username);
    // 중복되지 않는 email인지 검사
    boolean isUniqueEmail(String email);
}
