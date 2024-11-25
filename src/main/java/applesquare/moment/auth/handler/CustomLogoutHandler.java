package applesquare.moment.auth.handler;

import applesquare.moment.auth.exception.TokenError;
import applesquare.moment.auth.exception.TokenException;
import applesquare.moment.auth.service.TokenBlacklistService;
import applesquare.moment.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 요청으로 들어온 Access 토큰, Refresh 토큰을 블랙리스트에 등록
        // 응답으로 만료된 Refresh 쿠키 전달

        // SecurityContext 비우기
        SecurityContextHolder.clearContext();

        // 토큰 블랙리스트 등록
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Access 토큰이 존재하고
        if (authorizationHeader != null) {
            // Bearer 토큰이라면
            if(authorizationHeader.startsWith("Bearer ")){
                // Access 토큰을 블랙리스트에 등록
                String accessToken = authorizationHeader.substring(7);
                tokenBlacklistService.blacklist(accessToken, "logout");
            }
            else{
                // Bearer 토큰이 아니라면 예외 처리
                throw new TokenException(TokenError.UNSUPPORTED);
            }
        }

        // Refresh 토큰이 있다면
        String refreshToken= RequestUtil.getRefreshTokenFromRequest(request);
        if(refreshToken!=null){
            // Refresh 토큰을 블랙리스트에 등록
            tokenBlacklistService.blacklist(refreshToken, "logout");
        }
    }
}

