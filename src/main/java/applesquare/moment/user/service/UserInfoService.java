package applesquare.moment.user.service;

import applesquare.moment.user.dto.UserInfoReadResponseDTO;

public interface UserInfoService {
    int USER_ID_LENGTH=15;
    int MIN_NICKNAME_LENGTH=2;
    int MAX_NICKNAME_LENGTH=20;

    UserInfoReadResponseDTO readById(String id);
}
