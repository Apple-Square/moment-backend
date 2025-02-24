package applesquare.moment.notification.strategy;

import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.Notification;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.model.UserNotification;
import applesquare.moment.notification.repository.UserNotificationRepository;
import applesquare.moment.notification.service.NotificationSender;
import applesquare.moment.notification.service.NotificationService;
import applesquare.moment.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostLikeNotificationStrategy implements NotificationStrategy<Void> {
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationSender notificationSender;
    private final NotificationService notificationService;


    @Override
    public NotificationType getSupportedType(){
        return NotificationType.POST_LIKE;
    }

    /**
     * 게시물 작성자에게 좋아요 알림 전송
     * @param notificationRequestDTO 알림 전송 정보
     */
    @Override
    public void saveAndsendNotification(NotificationRequestDTO<Void> notificationRequestDTO){
        UserInfo sender=notificationRequestDTO.getSender();
        String receiverId=notificationRequestDTO.getReceiverId();

        // Notification 엔티티 생성
        Notification notification=Notification.builder()
                .type(NotificationType.POST_LIKE)
                .sender(sender)
                .content(String.format("%s님이 당신의 게시물을 좋아합니다.", sender.getNickname()))
                .referenceId(notificationRequestDTO.getReferenceId())
                .build();

        // UserNotification 엔티티 생성
        UserNotification userNotification=UserNotification.builder()
                .receiverId(receiverId)
                .notification(notification)
                .build();

        // 알림 엔티티 DB 저장
        userNotificationRepository.save(userNotification);

        // 배지 알림 전송
        long badgeCount=notificationService.countUnreadNotifications(receiverId);  // 미확인 알림 개수 조회
        notificationSender.sendBadge(receiverId, badgeCount);
    }

    @Override
    public void resendMissedSseNotification(UserNotification userNotification){
        // 배지 알림은 모든 손실된 알림에 대해서 1번만 전송하면 되므로 제외
    }
}
