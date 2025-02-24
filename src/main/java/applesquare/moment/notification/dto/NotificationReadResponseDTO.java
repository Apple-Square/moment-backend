package applesquare.moment.notification.dto;


import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadResponseDTO {
    private Long id;  // UserNotification 엔티티의 ID와 일치
    private UserProfileReadResponseDTO sender;
    private NotificationType type;
    private String title;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime regDate;
    @Builder.Default
    private boolean isRead=false;
    private String referenceId;
    @Builder.Default
    private List<NotificationLink> links=List.of();
}