package applesquare.moment.sse.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SseEvent {
    CONNECTION("connection"),   // SSE 연결 설정 이벤트
    NOTIFICATION_BADGE("notification.badge"),   // 일반 배지 알림 이벤트
    NOTIFICATION_BADGE_CHAT("notification.badge.chat"), // 채팅 배지 알림 이벤트
    NOTIFICATION_POPUP("notification.popup");   // 팝업 알림 이벤트

    private final String eventName;
}
