package applesquare.moment.notification.service.impl;

import applesquare.moment.chat.repository.ChatMessageRepository;
import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.model.UserNotification;
import applesquare.moment.notification.service.NotificationSendService;
import applesquare.moment.notification.service.NotificationSender;
import applesquare.moment.notification.service.NotificationService;
import applesquare.moment.notification.strategy.NotificationStrategy;
import applesquare.moment.notification.strategy.registry.NotificationStrategyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationSendServiceImpl implements NotificationSendService {
    private final NotificationStrategyRegistry notificationStrategyRegistry;
    private final NotificationService notificationService;
    private final NotificationSender notificationSender;
    private final ChatMessageRepository chatMessageRepository;


    /**
     * 수신자에게 전달할 알림 생성 후 전송
     * @param notificationRequestDTO 알림 요청 정보
     */
    @Async("taskExecutor")
    public void notify(NotificationRequestDTO notificationRequestDTO){
        // 적절한 알림 전략 선택
        NotificationType notificationType=notificationRequestDTO.getType();
        NotificationStrategy strategy=notificationStrategyRegistry.getStrategy(notificationType);

        // 전략에 따라 알림 생성 후 전송
        strategy.saveAndsendNotification(notificationRequestDTO);
    }

    /**
     * SSE 재연결동안에 손실된 알림 재전송
     * (lastEventId 이후에 발생한 알림을 재전송합니다)
     * @param receiverId 수신자 ID
     * @param lastEventId 클라이언트가 가장 최근에 받은 사용자 알림 ID
     */
    @Async("taskExecutor")
    public void resendMissedNotifications(String receiverId, Long lastEventId){
        // 일반 배지 알림 갱신
        long badgeCount=notificationService.countUnreadNotifications(receiverId);  // 미확인 알림 개수 조회
        notificationSender.sendBadge(receiverId, badgeCount);

        // 채팅 배지 알림 갱신
        long chatBadgeCount=chatMessageRepository.countUnreadMessagesByUserId(receiverId);
        notificationSender.sendChatBadge(receiverId, chatBadgeCount);

        // 손실된 알림 목록 조회
        List<UserNotification> missedNotifications=notificationService.readMissedAll(receiverId, lastEventId);

        // 전략에 따라 손실된 알림 전송
        missedNotifications.forEach((userNotification)->{
            // 적절한 알림 전략 선택
            NotificationType notificationType=userNotification.getNotification().getType();
            NotificationStrategy strategy=notificationStrategyRegistry.getStrategy(notificationType);

            // 전략에 따라 손실된 알림 재전송
            strategy.resendMissedSseNotification(userNotification);
        });
    }
}
