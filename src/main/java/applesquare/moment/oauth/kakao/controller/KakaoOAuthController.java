package applesquare.moment.oauth.kakao.controller;


import applesquare.moment.common.url.UrlManager;
import applesquare.moment.common.url.UrlPath;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth/kakao")
public class KakaoOAuthController {
    private final OAuthService oAuthService;
    private final UrlManager urlManager;
    private final JwtUtil jwtUtil;

    // 카카오 로그인 환경 변수
    @Value("${kakao.client.id}")
    private String kakaoClientId;


    /**
     * 카카오 로그인 API
     * (카카오 로그인 페이지 URL 전달)
     *
     * @return  (status) 200,
     *          (body) 카카오 로그인 URL
     */
    @GetMapping("/login")
    public ResponseEntity<String> redirectToKakaoAuth(){
        // 카카오 로그인 화면으로 리다이렉트
        String kakaoLoginUrl=urlManager.getUrl(UrlPath.KAKAO_LOGIN_URL);
        String kakaoLoginRedirectUri=urlManager.getUrl(UrlPath.KAKAO_LOGIN_REDIRECT_URI);
        String url = new StringBuilder(kakaoLoginUrl)
                .append("?client_id=").append(kakaoClientId)
                .append("&redirect_uri=").append(kakaoLoginRedirectUri)
                .append("&response_type=code")
                .toString();

        return ResponseEntity.status(HttpStatus.OK).body(url);
   }

    /**
     * 카카오 로그인 콜백 API
     * (인가 코드를 받아 로그인 처리를 완료시킴)
     *
     * @param code 인가 코드
     * @return  (status) 302,
     *          (header > Authorization) Access 토큰
     *          (header > Set-Cookie) Refresh 토큰
     *          (header > Location) 성공한 경우 -> moment 메인 페이지 URL
     *          (header > Location) 에러난 경우 -> moment 500 에러 페이지 URL
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code){
        try{
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

            // Location 설정
            headers.add(HttpHeaders.LOCATION, urlManager.getUrl(UrlPath.FRONT_MAIN_PAGE));

            return ResponseEntity.status(HttpStatus.FOUND)
                    .headers(headers)
                    .build();
        } catch (Exception e){
            e.printStackTrace();

            // 실패 했으면, 500 에러 페이지로 리다이렉트

            // Location 설정
            HttpHeaders headers=new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, urlManager.getUrl(UrlPath.FRONT_ERROR_500_PAGE));

            return ResponseEntity.status(HttpStatus.FOUND)
                    .headers(headers)
                    .build();
        }
    }
}
