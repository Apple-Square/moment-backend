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

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final KakaoAddressService kakaoAddressService;

    /**
     * 키워드에 따라 장소 검색
     * @param kakaoLocationSearchRequestDTO 검색 요청 정보
     * @return 장소 검색 결과
     */
    @Override
    public PageResponseDTO<AddressSearchResponseDTO> search(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO) {
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

    /**
     * 주소에 따라 장소 조회
     * @param address 주소
     * @return (주소가 실존하는 경우) 장소,
     *                  (주소가 실존하지 않는 경우) null
     */
    @Override
    public AddressSearchResponseDTO readByAddress(String address){
        // 요청 DTO 구성
        KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO=KakaoLocationSearchRequestDTO.builder()
                .keyword(address)
                .page(1)
                .size(1)
                .build();

        // Kakao 주소 검색 API 요청
        KakaoLocationSearchResponseDTO kakaoLocationSearchResponseDTO =kakaoAddressService.searchLocationByKeyword(kakaoLocationSearchRequestDTO);
        Meta meta= kakaoLocationSearchResponseDTO.getMeta();
        List<Document> documents= kakaoLocationSearchResponseDTO.getDocuments();

        // 검색 결과 반환
        if(documents.size()>0){
            Document document=documents.get(0);
            return AddressSearchResponseDTO.builder()
                    .id(document.getId())
                    .placeName(document.getPlaceName())
                    .addressName(document.getAddressName())
                    .roadAddressName(document.getRoadAddressName())
                    .categoryName(document.getCategoryName())
                    .x(document.getX())
                    .y(document.getY())
                    .build();
        }
        else return null;
    }
}
