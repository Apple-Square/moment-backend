package applesquare.moment.notification.service.impl;

import applesquare.moment.notification.dto.NotificationReadResponseDTO;
import applesquare.moment.notification.service.NotificationSender;
import applesquare.moment.sse.dto.SseSendRequestDTO;
import applesquare.moment.sse.service.SseCategory;
import applesquare.moment.sse.service.SseEvent;
import applesquare.moment.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSenderImpl implements NotificationSender {
    private final SseService sseService;


    /**
     * 미확인 알림 개수 전송
     * (실시간 배지 알림 전송)
     *
     * @param receiverId 수신자 ID
     * @param badgeCount 미확인 알림 개수
     */
    @Override
    public void sendBadge(String receiverId, long badgeCount){
        // SSE 전송
        SseSendRequestDTO sseSendRequestDTO=SseSendRequestDTO.builder()
                .sseCategory(SseCategory.NOTIFICATION)
                .receiverId(receiverId)
                .sseEvent(SseEvent.NOTIFICATION_BADGE)
                .data(badgeCount)
                .build();
        sseService.sendEvent(sseSendRequestDTO);
    }

    /**
     * 미확인 채팅 메시지 개수 전송
     * (실시간 채팅 배지 알림 전송)
     * @param receiverId 수신자 ID
     * @param badgeCount 미확인 채팅 메시지 개수
     */
    public void sendChatBadge(String receiverId, long badgeCount){
        // SSE 전송
        SseSendRequestDTO sseSendRequestDTO=SseSendRequestDTO.builder()
                .sseCategory(SseCategory.NOTIFICATION)
                .receiverId(receiverId)
                .sseEvent(SseEvent.NOTIFICATION_BADGE_CHAT)
                .data(badgeCount)
                .build();
        sseService.sendEvent(sseSendRequestDTO);
    }

    /**
     * 실시간 팝업 알림 전송
     * @param receiverId 사용자 ID
     * @param notificationDTO 알림 정보
     */
    @Override
    public void sendPopup(String receiverId, NotificationReadResponseDTO notificationDTO){
        // SSE 전송
        SseSendRequestDTO sseSendRequestDTO=SseSendRequestDTO.builder()
                .sseCategory(SseCategory.NOTIFICATION)
                .receiverId(receiverId)
                .lastEventId(notificationDTO.getId().toString())
                .sseEvent(SseEvent.NOTIFICATION_POPUP)
                .data(notificationDTO)
                .build();
        sseService.sendEvent(sseSendRequestDTO);
    }
}
