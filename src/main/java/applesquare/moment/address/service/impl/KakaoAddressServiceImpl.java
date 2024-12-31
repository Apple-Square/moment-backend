package applesquare.moment.address.service.impl;

import applesquare.moment.address.dto.KakaoAddressSearchRequestDTO;
import applesquare.moment.address.dto.KakaoAddressSearchResponseDTO;
import applesquare.moment.address.service.KakaoAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoAddressServiceImpl implements KakaoAddressService {
    private final RestTemplate restTemplate;
    @Value("${kakao.client.id}")
    private String kakaoClientId;
    private final String KAKAO_ADDRESS_URL="https://dapi.kakao.com/v2/local/search/address.json";


    /**
     * 카카오 API를 이용해서 키워드에 따라 주소 검색
     * @param kakaoAddressSearchRequestDTO 검색 요청 정보
     * @return 주소 검색 결과
     */
    @Override
    public KakaoAddressSearchResponseDTO searchAddressByKeyword(KakaoAddressSearchRequestDTO kakaoAddressSearchRequestDTO){
        String keyword = kakaoAddressSearchRequestDTO.getKeyword();
        String analyzeType=kakaoAddressSearchRequestDTO.getAnalyzeType();
        Integer page = kakaoAddressSearchRequestDTO.getPage();
        Integer size = kakaoAddressSearchRequestDTO.getSize();

        // URL 생성
        StringBuilder sb=new StringBuilder(KAKAO_ADDRESS_URL).append("?query=").append(keyword);
        if (analyzeType!= null) sb.append("&analyze_type=").append(analyzeType);
        if (page != null) sb.append("&page=").append(page);
        if (size != null) sb.append("&size=").append(size);
        String url=sb.toString();

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoClientId);

        // http 요청 (카카오 주소 검색 API)
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<KakaoAddressSearchResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KakaoAddressSearchResponseDTO.class);

        return response.getBody();
    }
}
