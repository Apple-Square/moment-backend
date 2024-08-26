package applesquare.moment.auth.service;

import applesquare.moment.auth.dto.UserCreateRequestDTO;

public interface AuthService {
    int MIN_USERNAME_LENGTH=8;
    int MAX_USERNAME_LENGTH=20;
    int MIN_PASSWORD_LENGTH=10;
    int MAX_PASSWORD_LENGTH=20;


    void createUser(UserCreateRequestDTO userCreateRequestDTO);
    boolean isUniqueUsername(String username);
    boolean isUniqueEmail(String email);
}
