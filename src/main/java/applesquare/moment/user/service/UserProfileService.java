package applesquare.moment.user.service;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserProfileService {
    String DEFAULT_PROFILE_NAME="default-profile.png";


    UserProfileReadResponseDTO readMyProfile();
    UserProfileReadResponseDTO readProfileById(String userId);

    String updateProfileImage(String userId, MultipartFile profileImage) throws Exception;
    void deleteProfileImage(String userId) throws IOException;

    PageResponseDTO<UserProfileReadResponseDTO> search(PageRequestDTO pageRequestDTO);
}
