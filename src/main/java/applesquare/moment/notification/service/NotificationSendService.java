package applesquare.moment.notification.service;

import applesquare.moment.notification.dto.NotificationRequestDTO;

public interface NotificationSendService {
    void notify(NotificationRequestDTO notificationRequestDTO);
    void resendMissedNotifications(String userId, Long lastEventId);
}
