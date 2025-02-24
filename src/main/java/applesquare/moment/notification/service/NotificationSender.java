package applesquare.moment.notification.service;

import applesquare.moment.notification.dto.NotificationReadResponseDTO;

public interface NotificationSender {
    void sendBadge(String receiverId, long badgeCount);
    void sendChatBadge(String receiverId, long badgeCount);
    void sendPopup(String receiverId, NotificationReadResponseDTO notificationDTO);
}
