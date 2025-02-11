package applesquare.moment.notification.service;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.notification.dto.NotificationReadResponseDTO;
import applesquare.moment.notification.model.UserNotification;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationService {
    // Entity -> DTO 변환
    NotificationReadResponseDTO toNotificationReadResponseDTO(UserNotification userNotification);
    // 알림 목록 조회
    PageResponseDTO<NotificationReadResponseDTO> readAll(String userId, PageRequestDTO pageRequestDTO);
    // 알림 읽음 처리
    void setAsRead(Long userNotificationId);
    // 미확인 알림 개수 조회
    long countUnreadNotifications(String userId);
    // 특정 사용자 알림 삭제
    void delete(Long userNotificationId);
    // 손실된 알림 목록 조회
    List<UserNotification> readMissedAll(String receiverId, Long lastEventId);
    // 알림 보관 기간이 지난 오래된 알림 삭제
    CompletableFuture<Long> deleteOldNotifications(int retentionDays);
}
