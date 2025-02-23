package applesquare.moment.config;

import applesquare.moment.auth.service.TokenBlacklistService;
import applesquare.moment.util.JwtUtil;
import applesquare.moment.websocket.handler.WebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // WebSocket 연결할 때 사용할 엔드 포인트 (handshake)
                .addInterceptors(new WebSocketHandshakeInterceptor(jwtUtil, tokenBlacklistService))  // JWT 인증을 위한 인터셉터 등록
                .setAllowedOriginPatterns("*") // 실제 운영 시에는 구체적인 도메인만 허용
                .withSockJS();  // SockJS 폴백 활성화
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 경로
        registry.enableSimpleBroker("/topic");
        // 클라이언트가 메시지 전송 시 사용하는 prefix
        registry.setApplicationDestinationPrefixes("/send");
    }
}