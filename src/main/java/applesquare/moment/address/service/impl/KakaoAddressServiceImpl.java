package applesquare.moment.address.service.impl;

import applesquare.moment.address.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.address.dto.KakaoLocationSearchResponseDTO;
import applesquare.moment.address.service.KakaoAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class KakaoAddressServiceImpl implements KakaoAddressService {
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    private final RestTemplate restTemplate;

    @Override
    public KakaoLocationSearchResponseDTO searchLocationByKeyword(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO) throws UnsupportedEncodingException {
        try{
            String keyword= kakaoLocationSearchRequestDTO.getKeyword();
            Integer page= kakaoLocationSearchRequestDTO.getPage();
            Integer size= kakaoLocationSearchRequestDTO.getSize();

            if(keyword==null){
                new IllegalArgumentException("키워드를 입력해주세요.");
            }

            // URL 생성
            String encodedKeyword=URLEncoder.encode(keyword, "UTF-8");
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + encodedKeyword;
            if(page!=null) url += "&page="+page;
            if(size!=null) url += "&size="+size;


            // 헤더 설정
            HttpHeaders headers=new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            // http 요청 (카카오 주소 검색 API)
            HttpEntity<String> httpEntity=new HttpEntity<>(headers);
            log.info("url : "+url);
            log.info("header : "+httpEntity.getHeaders());
            log.info("body : "+httpEntity.getBody());

            ResponseEntity<KakaoLocationSearchResponseDTO> response=restTemplate.exchange(url, HttpMethod.GET, httpEntity, KakaoLocationSearchResponseDTO.class);

            log.info("documentSize : "+response.getBody().getDocuments());

            return response.getBody();
        }catch(Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }
}
