package applesquare.moment.post.service;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.post.dto.PostDetailReadAllResponseDTO;
import applesquare.moment.post.dto.PostThumbnailReadAllResponseDTO;

public interface PostSearchService {
    // 게시물 검색 > 1열 피드
    PageResponseDTO<PostDetailReadAllResponseDTO> searchDetail(PageRequestDTO pageRequestDTO);
    PageResponseDTO<PostDetailReadAllResponseDTO> searchDetailByTag(PageRequestDTO pageRequestDTO);

    // 게시물 검색 > 3열 피드
    PageResponseDTO<PostThumbnailReadAllResponseDTO> searchThumbnail(PageRequestDTO pageRequestDTO);
    PageResponseDTO<PostThumbnailReadAllResponseDTO> searchThumbnailByTag(PageRequestDTO pageRequestDTO);
}
