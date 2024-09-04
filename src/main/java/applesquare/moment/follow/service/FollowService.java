package applesquare.moment.follow.service;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.follow.dto.FolloweeReadAllResponseDTO;
import applesquare.moment.follow.dto.FollowerReadAllResponseDTO;

public interface FollowService {
    String follow(String followeeId);
    String unfollow(String followeeId);

    PageResponseDTO<FollowerReadAllResponseDTO> readFollowerPage(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<FolloweeReadAllResponseDTO> readFollowingPage(String userId, PageRequestDTO pageRequestDTO);
}
