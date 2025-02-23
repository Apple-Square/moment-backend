package applesquare.moment.chat.dto;

import applesquare.moment.chat.model.ChatMessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageCreateRequestDTO {
    @NotNull(message = "메시지 전송 타입을 지정해주세요.")
    private ChatMessageType type;   // 메시지 전송 타입
    @Size(min=1, message = "메시지를 입력해주세요.")
    private String content; // 메시지 전송 내용
    private List<Long> fileIds; // 전송 파일 ID
    private Long postShareId;   // 공유 게시물 ID
}
