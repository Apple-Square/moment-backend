package applesquare.moment.notification.strategy;

import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.model.UserNotification;

public interface NotificationStrategy {
    NotificationType getSupportedType();
    // 알림 정보를 저장하고 전송한다.
    void saveAndsendNotification(NotificationRequestDTO notificationRequestDTO);
    // SSE 연결이 끊긴 동안 전송되지 못한 알림을 재전송한다.
    void resendMissedSseNotification(UserNotification userNotification);
}
