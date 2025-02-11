package applesquare.moment.location.service.impl;

import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.location.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.location.dto.KakaoLocationSearchResponseDTO;
import applesquare.moment.location.dto.KakaoLocationSearchResponseDTO.Document;
import applesquare.moment.location.dto.KakaoLocationSearchResponseDTO.Meta;
import applesquare.moment.location.dto.LocationSearchResponseDTO;
import applesquare.moment.location.service.KakaoLocationService;
import applesquare.moment.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final KakaoLocationService kakaoLocationService;

    /**
     * 키워드에 따라 장소 검색
     * @param kakaoLocationSearchRequestDTO 검색 요청 정보
     * @return 장소 검색 결과
     */
    @Override
    public PageResponseDTO<LocationSearchResponseDTO> searchLocation(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO) {
        // Kakao 장소 검색 API 요청
        KakaoLocationSearchResponseDTO kakaoLocationSearchResponseDTO =kakaoLocationService.searchLocationByKeyword(kakaoLocationSearchRequestDTO);
        Meta meta= kakaoLocationSearchResponseDTO.getMeta();
        List<Document> documents= kakaoLocationSearchResponseDTO.getDocuments();

        // DTO 변환
        List<LocationSearchResponseDTO> locationSearchResponseDTOS =documents.stream().map((document) -> {
            return LocationSearchResponseDTO.builder()
                    .id(document.getId())
                    .placeName(document.getPlaceName())
                    .addressName(document.getAddressName())
                    .roadAddressName(document.getRoadAddressName())
                    .categoryName(document.getCategoryName())
                    .x(document.getX())
                    .y(document.getY())
                    .build();
        }).toList();

        // 페이지 응답 생성
        PageResponseDTO<LocationSearchResponseDTO> pageResponseDTO=PageResponseDTO.<LocationSearchResponseDTO>builder()
                .content(locationSearchResponseDTOS)
                .hasNext(!meta.isEnd())
                .totalCount(meta.getTotalCount())
                .build();

        return pageResponseDTO;
    }
}