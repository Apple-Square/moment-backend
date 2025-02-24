package applesquare.moment.notification.dto;

import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.user.model.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO<T> {
    private NotificationType type;  // 알림 유형
    private UserInfo sender; // 송신자 정보
    private String receiverId; // 단일 수신자 ID
    private String referenceId;  // 래퍼런스 ID
    private T referenceObject;  // 래퍼런스 객체 (알림 처리에 필요한 객체 전달)
}
