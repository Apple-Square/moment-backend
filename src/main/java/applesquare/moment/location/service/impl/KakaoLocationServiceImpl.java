package applesquare.moment.location.service.impl;

import applesquare.moment.location.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.location.dto.KakaoLocationSearchResponseDTO;
import applesquare.moment.location.service.KakaoLocationService;
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
public class KakaoLocationServiceImpl implements KakaoLocationService {
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    private final RestTemplate restTemplate;

    /**
     * 카카오 API를 이용해서 키워드에 따라 장소 검색
     * @param kakaoLocationSearchRequestDTO 검색 요청 정보
     * @return 장소 검색 결과
     */
    @Override
    public KakaoLocationSearchResponseDTO searchLocationByKeyword(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO) {
        String keyword = kakaoLocationSearchRequestDTO.getKeyword();
        Integer page = kakaoLocationSearchRequestDTO.getPage();
        Integer size = kakaoLocationSearchRequestDTO.getSize();

        // URL 생성
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + keyword;
        if (page != null) url += "&page=" + page;
        if (size != null) url += "&size=" + size;

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        // http 요청 (카카오 주소 검색 API)
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<KakaoLocationSearchResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, KakaoLocationSearchResponseDTO.class);

        return response.getBody();
    }
}

