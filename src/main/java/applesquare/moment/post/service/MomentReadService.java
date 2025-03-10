package applesquare.moment.post.service;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.post.dto.MomentDetailReadAllResponseDTO;
import applesquare.moment.post.dto.MomentThumbnailReadAllResponseDTO;

public interface MomentReadService {
    PageResponseDTO<MomentDetailReadAllResponseDTO> readDetailAll(PageRequestDTO pageRequestDTO);
    PageResponseDTO<MomentDetailReadAllResponseDTO> readDetailAllByUser(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<MomentThumbnailReadAllResponseDTO> readThumbnailAllByUser(String userId, PageRequestDTO pageRequestDTO);
    PageResponseDTO<MomentDetailReadAllResponseDTO> readLikedDetailAllByUser(String userId, PageRequestDTO pageRequestDTO);
}
