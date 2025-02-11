package applesquare.moment.follow.service;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.follow.dto.FollowReadAllResponseDTO;

public interface FollowService {
    String follow(String followeeId);
    String unfollow(String followeeId);

    PageResponseDTO<FollowReadAllResponseDTO> searchFollowerByKeyword(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<FollowReadAllResponseDTO> searchFollowingByKeyword(String userId, PageRequestDTO pageRequestDTO);
}
