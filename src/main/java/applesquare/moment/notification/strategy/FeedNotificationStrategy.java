package applesquare.moment.notification.strategy;

import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.Notification;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.model.UserNotification;
import applesquare.moment.notification.repository.UserNotificationRepository;
import applesquare.moment.notification.service.NotificationSender;
import applesquare.moment.notification.service.NotificationService;
import applesquare.moment.user.model.UserInfo;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class FeedNotificationStrategy implements NotificationStrategy<Void>{
    private final UserNotificationRepository userNotificationRepository;
    private final FollowRepository followRepository;
    private final NotificationSender notificationSender;
    private final NotificationService notificationService;
    private final ThreadPoolTaskExecutor taskExecutor;


    @Override
    public NotificationType getSupportedType(){
        return NotificationType.FEED;
    }

    /**
     * 팔로워들에게 피드 알림 전송 (비동기 처리)
     * @param notificationRequestDTO 알림 요청 정보
     */
    @Override
    public void saveAndsendNotification(NotificationRequestDTO<Void> notificationRequestDTO){
        UserInfo sender=notificationRequestDTO.getSender();

        // Notification 엔티티 생성
        Notification notification=Notification.builder()
                .type(NotificationType.FEED)
                .sender(sender)
                .content(String.format("%s님이 새로운 게시물을 올렸습니다.", sender.getNickname()))
                .referenceId(notificationRequestDTO.getReferenceId())
                .build();

        // 팔로워들에게 피드 알림 전송
        int pageSize = 1000;
        Long cursor = null;

        while (true) {
            Pageable pageable=PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "id"));
            List<Tuple> tuples = followRepository.findAllFollowerByUserId(sender.getId(), cursor, pageable);

            if (tuples.isEmpty()) {
                break;
            }

            List<UserNotification> userNotifications=tuples.stream().map((tuple)->
                UserNotification.builder()
                        .receiverId((String) tuple.get("followerId"))
                        .notification(notification)
                        .build()
            ).toList();

            // 사용자 알림 엔티티 DB 저장
            userNotificationRepository.saveAll(userNotifications);

            // 동기적으로 각 팔로워 처리
            taskExecutor.execute(()->{
                userNotifications.forEach((userNotification) -> {
                    String receiverId=userNotification.getReceiverId();

                    log.info(receiverId+"에게 SSE 전송");

                    // 배지 알림 전송
                    long badgeCount=notificationService.countUnreadNotifications(receiverId);  // 미확인 알림 개수 조회
                    notificationSender.sendBadge(receiverId, badgeCount);
                });
            });

            cursor = (Long) tuples.get(tuples.size() - 1).get("followId");
        }
    }

    @Override
    public void resendMissedSseNotification(UserNotification userNotification){
        // 배지 알림은 모든 손실된 알림에 대해서 1번만 전송하면 되므로 제외
    }
}
