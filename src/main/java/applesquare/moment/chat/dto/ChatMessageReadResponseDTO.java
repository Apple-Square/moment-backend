package applesquare.moment.chat.dto;

import applesquare.moment.chat.model.ChatMessageType;
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
public class ChatMessageReadResponseDTO {
    private Long id;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime regDate;
    private String senderId;
    private ChatMessageType type;
    private String content;
    private List<String> fileUrls;
    private SharedPostReadResponseDTO sharedPost;
    private long unreadCount;
    private boolean isDeleted;
}
