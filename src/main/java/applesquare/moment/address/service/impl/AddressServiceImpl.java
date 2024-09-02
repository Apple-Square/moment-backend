package applesquare.moment.address.service.impl;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.dto.KakaoAddressSearchRequestDTO;
import applesquare.moment.address.dto.KakaoAddressSearchResponseDTO;
import applesquare.moment.address.dto.KakaoAddressSearchResponseDTO.Document;
import applesquare.moment.address.service.AddressService;
import applesquare.moment.address.service.KakaoAddressService;
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
     * 키워드에 따라 주소 검색
     * @param keyword 키워드
     * @return (주소가 실존하는 경우) 주소
     *                  (주소가 실존하지 않는 경우) null
     */
    @Override
    public AddressSearchResponseDTO searchAddress(String keyword){
        // 요청 DTO 구성
        KakaoAddressSearchRequestDTO kakaoAddressSearchRequestDTO=KakaoAddressSearchRequestDTO.builder()
                .keyword(keyword)
                .analyzeType("exact")
                .page(1)
                .size(1)
                .build();

        // Kakao 주소 검색 API 요청
        KakaoAddressSearchResponseDTO kakaoAddressSearchResponseDTO =kakaoAddressService.searchAddressByKeyword(kakaoAddressSearchRequestDTO);
        List<Document> documents= kakaoAddressSearchResponseDTO.getDocuments();

        // 검색 결과 반환
        if(documents.size()>0){
            Document document=documents.get(0);
            return AddressSearchResponseDTO.builder()
                    .addressName(document.getAddressName())
                    .x(document.getX())
                    .y(document.getY())
                    .build();
        }
        else return null;
    }
}
