package applesquare.moment.user.service;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserProfileService {
    String DEFAULT_PROFILE_NAME="default-profile.png";

    UserProfileReadResponseDTO toUserProfileDTO(UserInfo userInfo);

    UserProfileReadResponseDTO readMyProfile();
    UserProfileReadResponseDTO readProfileById(String userId);

    String updateProfileImage(String userId, MultipartFile profileImage) throws Exception;
    void deleteProfileImage(String userId) throws IOException;

    PageResponseDTO<UserProfileReadResponseDTO> search(PageRequestDTO pageRequestDTO);
}
