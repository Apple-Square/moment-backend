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
public class NotificationRequestDTO {
    private NotificationType type;  // 알림 유형
    private UserInfo sender; // 송신자 정보
    private String receiverId; // 단일 수신자 ID
    private String referenceId;  // 래퍼런스 ID
}
