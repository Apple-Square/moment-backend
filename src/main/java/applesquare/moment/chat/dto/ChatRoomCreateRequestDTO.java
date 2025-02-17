package applesquare.moment.chat.dto;

import applesquare.moment.chat.model.ChatRoomType;
import applesquare.moment.chat.service.ChatRoomService;
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
public class ChatRoomCreateRequestDTO {
    private String roomName;    // 채팅방 이름
    @NotNull(message = "채팅방 유형을 입력해주세요.")
    private ChatRoomType roomType;  // 채팅방 유형
    @NotNull
    @Size(min = ChatRoomService.MIN_MEMBER_COUNT,
            max = ChatRoomService.MAX_MEMBER_COUNT,
            message = "채팅방에는 ("+ChatRoomService.MIN_MEMBER_COUNT+" ~ "+ChatRoomService.MAX_MEMBER_COUNT+")명의 멤버가 필요합니다.")
    private List<String> memberIds;    // 채팅방 멤버 ID 목록
}
