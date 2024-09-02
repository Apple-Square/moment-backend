package applesquare.moment.address.service.impl;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.address.dto.KakaoLocationSearchResponseDTO;
import applesquare.moment.address.dto.KakaoLocationSearchResponseDTO.Document;
import applesquare.moment.address.dto.KakaoLocationSearchResponseDTO.Meta;
import applesquare.moment.address.service.AddressService;
import applesquare.moment.address.service.KakaoAddressService;
import applesquare.moment.common.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final KakaoAddressService kakaoAddressService;

    @Override
    public PageResponseDTO<AddressSearchResponseDTO> search(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO) throws UnsupportedEncodingException {
        // Kakao 주소 검색 API 요청
        KakaoLocationSearchResponseDTO kakaoLocationSearchResponseDTO =kakaoAddressService.searchLocationByKeyword(kakaoLocationSearchRequestDTO);
        Meta meta= kakaoLocationSearchResponseDTO.getMeta();
        List<Document> documents= kakaoLocationSearchResponseDTO.getDocuments();

        // DTO 변환
        List<AddressSearchResponseDTO> addressSearchResponseDTOS =documents.stream().map((document) -> {
            return AddressSearchResponseDTO.builder()
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
        PageResponseDTO pageResponseDTO=PageResponseDTO.<AddressSearchResponseDTO>builder()
                .content(addressSearchResponseDTOS)
                .hasNext(!meta.isEnd())
                .totalCount(meta.getTotalCount())
                .build();

        return pageResponseDTO;
    }
}
