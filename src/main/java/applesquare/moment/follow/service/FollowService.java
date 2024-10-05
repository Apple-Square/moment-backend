package applesquare.moment.follow.service;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.follow.dto.FollowReadAllResponseDTO;

public interface FollowService {
    String follow(String followeeId);
    String unfollow(String followeeId);

    PageResponseDTO<FollowReadAllResponseDTO> searchFollowerByKeyword(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<FollowReadAllResponseDTO> searchFollowingByKeyword(String userId, PageRequestDTO pageRequestDTO);
}
