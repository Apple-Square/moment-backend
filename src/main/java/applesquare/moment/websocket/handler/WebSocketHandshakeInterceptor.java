package applesquare.moment.websocket.handler;

import applesquare.moment.auth.service.TokenBlacklistService;
import applesquare.moment.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Collections;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Authorization 헤더 추출
        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            if (jwtUtil.validateToken(token) && !tokenBlacklistService.exists(token)) {
                // 토큰에서 사용자 ID 추출
                String userId = jwtUtil.getSubjectFromToken(token);

                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

                // WebSocket의 세션 속성에 인증 정보 저장
                attributes.put("SECUTIRY_CONTEXT", authentication);
                return true;
            }
        }

        // 인증 실패 시 연결 거부
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        log.info("WebSocket Handshake Successful!");
    }
}
