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
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    private final RestTemplate restTemplate;


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
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + keyword;
        if (analyzeType!= null) url += "&analyze_type=" + analyzeType;
        if (page != null) url += "&page=" + page;
        if (size != null) url += "&size=" + size;

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        // http 요청 (카카오 주소 검색 API)
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<KakaoAddressSearchResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KakaoAddressSearchResponseDTO.class);

        return response.getBody();
    }
}
