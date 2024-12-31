package applesquare.moment.oauth.naver.service.impl;

import applesquare.moment.oauth.naver.dto.NaverUserInfoReadResponseDTO;
import applesquare.moment.oauth.naver.service.NaverAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
@Transactional
@RequiredArgsConstructor
public class NaverAuthServiceImpl implements NaverAuthService {
    private final RestTemplate restTemplate;

    @Value("${naver.client.id}")
    private String naverClientId;
    @Value("${naver.client.secret}")
    private String naverClientSecret;
    private final String naverTokenUrl="https://nid.naver.com/oauth2.0/token";
    private final String naverUserInfoUrl="https://openapi.naver.com/v1/nid/me";


    /**
     * (인가 코드로) 네이버 액세스 토큰 획득
     * @param code 인가 코드
     * @param state 상태 토큰 (CSRF 방지 용도)
     * @return 네이버 액세스 토큰
     */
    @Override
    public String getAccessToken(String code, String state){
        // URL 생성
        String url=new StringBuilder(naverTokenUrl)
                .append("?grant_type=authorization_code")
                .append("&client_id=").append(naverClientId)
                .append("&client_secret=").append(naverClientSecret)
                .append("&code=").append(code)
                .append("&state=").append(state)
                .toString();

        // HTTP 요청 보내기
        HttpHeaders headers=new HttpHeaders();
        HttpEntity<String> naverTokenRequest=new HttpEntity<>(headers);
        ResponseEntity<Map> naverTokenResponse=restTemplate.exchange(
                url,
                HttpMethod.GET,
                naverTokenRequest,
                Map.class
        );

        // 액세스 토큰 추출
        if(naverTokenResponse.getStatusCode() != HttpStatus.OK){
            // 상태 코드가 200이 아니라면
            throw new OAuth2AuthenticationException("Naver 토큰 요청에 실패했습니다.");
        }

        Map<String, Object> responseBody=naverTokenResponse.getBody();
        if(responseBody==null){
            throw new OAuth2AuthenticationException("Naver 토큰 요청에 실패했습니다.");
        }

        String accessToken=(String) responseBody.get("access_token");
        if(accessToken==null){
            throw new OAuth2AuthenticationException("Naver 토큰 응답에 access_token이 존재하지 않습니다.");
        }

        return accessToken;
    }

    /**
     * (액세스 토큰으로) 네이버 사용자 정보 조회
     * @param accessToken 액세스 토큰
     * @return 네이버 사용자 정보
     */
    @Override
    public NaverUserInfoReadResponseDTO getUserInfoByToken(String accessToken){
        // HTTP Header 생성
        HttpHeaders headers=new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        // HTTP 요청 보내기
        String url=naverUserInfoUrl;
        HttpEntity<String> naverUserRequest=new HttpEntity<>(headers);
        ResponseEntity<NaverUserInfoReadResponseDTO> naverUserResponse=restTemplate.exchange(
                url,
                HttpMethod.GET,
                naverUserRequest,
                NaverUserInfoReadResponseDTO.class
        );

        return naverUserResponse.getBody();
    }
}
