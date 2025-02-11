package applesquare.moment.sse.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SseEvent {
    CONNECTION("connection"),
    NOTIFICATION_BADGE("notification.badge"),
    NOTIFICATION_POPUP("notification.popup");

    private final String eventName;
}
