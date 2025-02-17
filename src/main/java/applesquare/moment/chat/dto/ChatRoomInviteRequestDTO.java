package applesquare.moment.chat.dto;

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
public class ChatRoomInviteRequestDTO {
    @NotNull(message = "채팅방으로 초대할 사용자 ID를 입력해주세요.")
    @Size(min = 1, message = "채팅방으로 초대할 사용자 ID를 입력해주세요.")
    List<String> memberIds;
}
