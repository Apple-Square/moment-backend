package applesquare.moment.chat.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageCreateRequestDTO {
    @Size(min=1, message = "메시지를 입력해주세요.")
    private String content;
}
