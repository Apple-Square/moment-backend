package applesquare.moment.follow.service;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;

public interface FollowService {
    String follow(String followeeId);
    String unfollow(String followeeId);

    PageResponseDTO<UserProfileReadResponseDTO> readFollowerPage(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<UserProfileReadResponseDTO> readFollowingPage(String userId, PageRequestDTO pageRequestDTO);
}
