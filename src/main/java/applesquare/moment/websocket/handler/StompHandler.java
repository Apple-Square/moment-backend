package applesquare.moment.websocket.handler;

import applesquare.moment.auth.exception.TokenError;
import applesquare.moment.auth.exception.TokenException;
import applesquare.moment.auth.service.TokenBlacklistService;
import applesquare.moment.util.JwtUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Collections;

@Component
@Log4j2
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public StompHandler(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // SEND 명령일 때 토큰 검증 수행
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

            // 토큰 받았는지 검사
            if(authorization==null){
                // 토큰을 받지 못했으면 예외 처리
                throw new TokenException(TokenError.UNACCEPT);
            }

            // Bearer 토큰인지 검사
            if(!authorization.startsWith("Bearer ")){
                // Bearer 토큰이 아니면 예외 처리
                throw new TokenException(TokenError.UNSUPPORTED);
            }

            // authrization 헤더에서 토큰 추출
            String token = authorization.substring(7);

            // 유효한 토큰인지 검사 (유효하지 않을 경우, validateToken() 내부에서 예외 던짐)
            if (jwtUtil.validateToken(token)) {
                // 블랙리스트 검사
                if (tokenBlacklistService.exists(token)) {
                    // 블랙 리스트에 등록되었다면 예외 처리
                    throw new TokenException(TokenError.BLACKLISTED);
                }

                // 토큰에서 사용자 ID 추출
                String userId = jwtUtil.getSubjectFromToken(token);

                // 인증 객체 생성
                Principal principal = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                accessor.setUser(principal);
            }
        }
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // 필요 시 후처리 로직 구현
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("Post Send: Command={}, SessionId={}", accessor.getCommand(), accessor.getSessionId());
    }
}
