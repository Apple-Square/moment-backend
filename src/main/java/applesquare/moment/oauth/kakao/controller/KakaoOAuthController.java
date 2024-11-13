package applesquare.moment.oauth.kakao.controller;


import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.oauth.service.OAuthService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth/kakao")
public class KakaoOAuthController {
    private final OAuthService oAuthService;
    private final JwtUtil jwtUtil;

    // 카카오 로그인 환경 변수
    @Value("${kakao.client.id}")
    private String kakaoClientId;
    @Value("${kakao.oauth.redirect-uri}")
    private String kakaoOauthRedirectUri;
    @Value("${kakao.login.url}")
    private String kakaoLoginUrl;


    /**
     * 카카오 로그인 API
     * (카카오 로그인 화면으로 리다이렉트)
     *
     * @return  (status) 302,
     *          (header) 카카오 로그인 URL
     */
    @GetMapping("/login")
    public ResponseEntity<Void> redirectToKakaoAuth(){
        // HTTP Header 생성
        HttpHeaders headers=new HttpHeaders();

        // 카카오 로그인 화면으로 리다이렉트
        String url = new StringBuilder(kakaoLoginUrl)
                .append("?client_id=").append(kakaoClientId)
                .append("&redirect_uri=").append(kakaoOauthRedirectUri)
                .append("&response_type=code")
                .toString();
        headers.setLocation(URI.create(url));

        return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers)
                .build();
    }

    /**
     * 카카오 로그인 콜백 API
     * (인가 코드를 받아 로그인 처리를 완료시킴)
     *
     * @param code 인가 코드
     * @return  (status) 200,
     *          (header) Access, Refresh 토큰
     *          (body) 소셜 유저 정보
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestParam String code){
        // 유저 프로필 조회
        UserProfileReadResponseDTO userProfileDTO=oAuthService.loginWithKakao(code);

        // HTTP Header 설정
        HttpHeaders headers=new HttpHeaders();

        // Access 토큰 발급
        String userId= userProfileDTO.getId();
        String accessToken = jwtUtil.generateAccessToken(userId);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        // Refresh 토큰 발급
        ResponseCookie refreshTokenCookie= jwtUtil.generateRefreshCookie(userId);
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // HTTP Body 설정
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "카카오 로그인에 성공했습니다.");
        responseMap.put("user", userProfileDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(responseMap.getMap());
    }
}
