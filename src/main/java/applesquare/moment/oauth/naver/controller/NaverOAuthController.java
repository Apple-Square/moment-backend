package applesquare.moment.oauth.naver.controller;

import applesquare.moment.common.service.StateService;
import applesquare.moment.exception.ResponseMap;
import applesquare.moment.oauth.service.OAuthService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth/naver")
public class NaverOAuthController{
    private final OAuthService oAuthService;
    private final StateService stateService;
    private final JwtUtil jwtUtil;

    // 네이버 로그인 환경 변수
    @Value("${naver.client.id}")
    private String naverClientId;
    @Value("${naver.login.url}")
    private String naverLoginUrl;
    @Value("${naver.oauth.redirect-uri}")
    private String naverOauthRedirectUri;


    /**
     * 네이버 로그인 API
     * (네이버 로그인 화면으로 리다이렉트)
     *
     * @return  (status) 302,
     *          (header) 네이버 로그인 URL
     */
    @GetMapping("/login")
    public ResponseEntity<Void> redirectToNaverAuth(){
        // CSRF 방지를 위해 CSRF 방지 토큰 생성
        String state= UUID.randomUUID().toString();
        String encodedState= URLEncoder.encode(state, StandardCharsets.UTF_8);

        // Redis에 CSRF 방지 토큰 등록
        stateService.create(encodedState, 10, TimeUnit.MINUTES);

        // HTTP Header 생성
        HttpHeaders headers=new HttpHeaders();

        // 네이버 로그인 화면으로 리다이렉트
        String url=new StringBuilder(naverLoginUrl)
                .append("?response_type=code")
                .append("&client_id=").append(naverClientId)
                .append("&redirect_uri=").append(naverOauthRedirectUri)
                .append("&state=").append(encodedState)
                .toString();
        headers.setLocation(URI.create(url));

        return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers)
                .build();
    }

    /**
     * 네이버 로그인 콜백 API
     * (인가 코드와 state를 받아 로그인 처리를 완료시킴)
     *
     * @param state 상태 토큰 (CSRF 방지 용도)
     * @param code 인증 코드
     * @param error 에러 코드
     * @param errorDescription 에러 메세지
     * @return  (statue) 200,
     *          (header) Access, Refresh 토큰
     *          (body) 소셜 유저 정보
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestParam(value = "state", required = true) String state,
                                                        @RequestParam(value = "code", required = false) String code,
                                                        @RequestParam(value = "error", required = false) String error,
                                                        @RequestParam(value = "error_description", required = false) String errorDescription){
        // CSRF 토큰 확인
        if(!stateService.exists(state)){
            // state 값이 다르다면
            throw new AccessDeniedException("유효하지 않은 요청입니다.");
        }

        // 네이버 로그인 과정에서 문제가 발생했다면 예외 처리
        if(error!=null){
            log.error("네이버 로그인 실패 : "+errorDescription);
            throw new OAuth2AuthenticationException("네이버 로그인에 실패했습니다.");
        }

        // 요청에 이미 사용된 CSRF 토큰을 삭제
        stateService.delete(state);

        // 유저 프로필 조회
        UserProfileReadResponseDTO userProfileDTO=oAuthService.loginWithNaver(code, state);

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
        responseMap.put("message", "네이버 로그인에 성공했습니다.");
        responseMap.put("user", userProfileDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(responseMap.getMap());
    }
}
