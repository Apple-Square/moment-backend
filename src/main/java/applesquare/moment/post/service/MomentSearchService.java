package applesquare.moment.post.service;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.post.dto.MomentDetailReadAllResponseDTO;

public interface MomentSearchService {
    // 게시글 검색 > 모먼츠 탭
    PageResponseDTO<MomentDetailReadAllResponseDTO> searchDetail(PageRequestDTO pageRequestDTO);
    PageResponseDTO<MomentDetailReadAllResponseDTO> searchDetailByTag(PageRequestDTO pageRequestDTO);
}
