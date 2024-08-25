package applesquare.moment.user.service;

import applesquare.moment.user.dto.UserInfoUpdateRequestDTO;
import applesquare.moment.user.dto.UserPageReadResponseDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserInfoService {
    String DEFAULT_PROFILE_NAME="default-profile.png";
    int USER_ID_LENGTH=15;
    int MIN_NICKNAME_LENGTH=2;
    int MAX_NICKNAME_LENGTH=20;

    UserProfileReadResponseDTO readMyProfile();
    UserProfileReadResponseDTO readProfileById(String userId);
    UserPageReadResponseDTO readUserPageById(String userId);

    String updateUserInfo(String userId, UserInfoUpdateRequestDTO userInfoUpdateRequestDTO);
    String updateProfileImage(String userId, MultipartFile profileImage);

    void deleteProfileImage(String userId) throws IOException;
}
