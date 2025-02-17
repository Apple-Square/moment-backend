package applesquare.moment.follow.service;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.follow.dto.FollowReadAllResponseDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;

public interface FollowService {
    String follow(String followeeId);
    String unfollow(String followeeId);

    PageResponseDTO<FollowReadAllResponseDTO> searchFollowerByKeyword(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<FollowReadAllResponseDTO> searchFollowingByKeyword(String userId, PageRequestDTO pageRequestDTO);

    // 특정 사용자와 상호 팔로우 관계에 있는 사용자 프로필 검색
    PageResponseDTO<UserProfileReadResponseDTO> searchMutualFollowerByKeyword(String userId, PageRequestDTO pageRequestDTO);
}
