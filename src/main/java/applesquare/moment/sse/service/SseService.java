package applesquare.moment.sse.service;

import applesquare.moment.sse.dto.SseSendRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    long SSE_TIMEOUT_MS = 30_000L;  // 30초

    // SSE 연결 생성
    SseEmitter connect(SseCategory sseCategory, String userId);
    // SSE 이벤트 전송
    void sendEvent(SseSendRequestDTO sseSendRequestDTO);
}
