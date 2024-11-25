package applesquare.moment.auth.handler;

import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
    private final JwtUtil jwtUtil;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 응답 구성
        response.setStatus(HttpServletResponse.SC_OK);

        // 만료된 Refresh 쿠키 설정
        response.addHeader(HttpHeaders.SET_COOKIE, jwtUtil.generateClearRefreshCookie().toString());

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "로그아웃에 성공했습니다.");

        response.getWriter().write(responseMap.toJson());
    }
}