package applesquare.moment.oauth.kakao.service.impl;

import applesquare.moment.common.url.UrlManager;
import applesquare.moment.common.url.UrlPath;
import applesquare.moment.oauth.kakao.dto.KakaoUserInfoReadResponseDTO;
import applesquare.moment.oauth.kakao.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
@Transactional
@RequiredArgsConstructor
public class KakaoAuthServiceImpl implements KakaoAuthService {
    private final UrlManager urlManager;
    private final RestTemplate restTemplate;

    @Value("${kakao.client.id}")
    private String kakaoClientId;


    /**
     * (인가 코드로) 카카오 액세스 토큰 획득
     * @param code 인가 코드
     * @return 카카오 액세스 토큰
     */
    @Override
    public String getAccessToken(String code){
        // HTTP Header 생성
        HttpHeaders headers=new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", urlManager.getUrl(UrlPath.KAKAO_LOGIN_REDIRECT_URI));
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest=new HttpEntity<>(body, headers);
        String kakaoTokenUrl= urlManager.getUrl(UrlPath.KAKAO_TOKEN_URL);
        ResponseEntity<Map> kakaoTokenResponse = restTemplate.exchange(
                kakaoTokenUrl,
                HttpMethod.POST,
                kakaoTokenRequest,
                Map.class);

        // 액세스 토큰 추출
        Map<String, Object> responseBody = kakaoTokenResponse.getBody();
        if(responseBody==null){
            throw new OAuth2AuthenticationException("Kakao 토큰 요청에 실패했습니다.");
        }

        String accessToken=(String) responseBody.get("access_token");
        if(accessToken==null){
            throw new OAuth2AuthenticationException("Kakao 토큰 응답에 access_token이 존재하지 않습니다.");
        }

        return accessToken;
    }

    /**
     * (액세스 토큰으로) 카카오 사용자 정보 조회
     * @param accessToken 액세스 토큰
     * @return 카카오 사용자 정보
     */
    @Override
    public KakaoUserInfoReadResponseDTO getUserInfoByToken(String accessToken){
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        // HTTP 요청 보내기
        HttpEntity<String> kakaoUserRequest = new HttpEntity<>(headers);
        String kakaoUserInfoUrl=urlManager.getUrl(UrlPath.KAKAO_USER_INFO_URL);
        ResponseEntity<KakaoUserInfoReadResponseDTO> kakaoUserResponse = restTemplate.exchange(
                kakaoUserInfoUrl,
                HttpMethod.GET,
                kakaoUserRequest,
                KakaoUserInfoReadResponseDTO.class
        );

        return kakaoUserResponse.getBody();
    }
}
