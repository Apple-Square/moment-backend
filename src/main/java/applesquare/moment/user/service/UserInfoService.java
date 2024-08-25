package applesquare.moment.user.service;

import applesquare.moment.user.dto.UserInfoUpdateRequestDTO;
import applesquare.moment.user.dto.UserPageReadResponseDTO;

public interface UserInfoService {
    int USER_ID_LENGTH=15;
    int MIN_NICKNAME_LENGTH=2;
    int MAX_NICKNAME_LENGTH=20;


    String updateUserInfo(String userId, UserInfoUpdateRequestDTO userInfoUpdateRequestDTO);
    UserPageReadResponseDTO readUserPageById(String userId);
}
