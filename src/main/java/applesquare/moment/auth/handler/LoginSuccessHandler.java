package applesquare.moment.auth.handler;

import applesquare.moment.auth.security.UserDetailsImpl;
import applesquare.moment.exception.ResponseMap;
import applesquare.moment.exception.TokenError;
import applesquare.moment.exception.TokenException;
import applesquare.moment.user.dto.UserInfoReadResponseDTO;
import applesquare.moment.user.service.UserInfoService;
import applesquare.moment.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Log4j2
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserInfoService userInfoService;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("Login Success Handler.........");

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            // 인증 정보를 받지 못 한 경우
            if(userDetails==null){
                throw new TokenException(TokenError.UNACCEPT);
            }

            // SecurityContext에 Authentication 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 응답 생성
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);

            // Access 토큰 발급
            String username=userDetails.getUsername();
            String accessToken = jwtUtil.generateAccessToken(username);
            response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

            // Refresh 토큰 발급
            ResponseCookie refreshTokenCookie= jwtUtil.generateRefreshCookie(username);
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            // 사용자 정보 조회
            String userId=userDetails.getId();
            UserInfoReadResponseDTO userInfoReadResponseDTO=userInfoService.readById(userId);

            // 응답 본문 구성
            ResponseMap responseMap=new ResponseMap();
            responseMap.put("message", "로그인에 성공했습니다.");
            responseMap.put("user", userInfoReadResponseDTO);

            // 응답 전달
            response.getWriter().write(responseMap.toJson());
        }
        catch(TokenException e){
            log.error("TokenException: " + e.getMessage());
            e.sendErrorResponse(response);
        }
    }
}
