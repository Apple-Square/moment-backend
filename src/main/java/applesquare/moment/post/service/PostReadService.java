package applesquare.moment.post.service;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.post.dto.PostDetailReadAllResponseDTO;
import applesquare.moment.post.dto.PostThumbnailReadAllResponseDTO;

public interface PostReadService {
    // 추천 게시물 목록 조회
    PageResponseDTO<PostDetailReadAllResponseDTO> readDetailAll(PageRequestDTO pageRequestDTO);
    PageResponseDTO<PostThumbnailReadAllResponseDTO> readThumbnailAll(PageRequestDTO pageRequestDTO);

    // 특정 유저가 작성한 게시물 목록 조회
    PageResponseDTO<PostDetailReadAllResponseDTO> readDetailAllByUser(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<PostThumbnailReadAllResponseDTO> readThumbnailAllByUser(String userId, PageRequestDTO pageRequestDTO);

    // 특정 유저가 좋아요 누른 게시물 목록 조회
    PageResponseDTO<PostDetailReadAllResponseDTO> readLikedDetailAllByUser(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<PostThumbnailReadAllResponseDTO> readLikedThumbnailAllByUser(String userId, PageRequestDTO pageRequestDTO);
}
