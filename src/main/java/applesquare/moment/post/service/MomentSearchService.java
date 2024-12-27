package applesquare.moment.post.service;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.post.dto.MomentDetailReadAllResponseDTO;

public interface MomentSearchService {
    // 게시글 검색 > 모먼츠 탭
    PageResponseDTO<MomentDetailReadAllResponseDTO> searchDetail(PageRequestDTO pageRequestDTO);
}
