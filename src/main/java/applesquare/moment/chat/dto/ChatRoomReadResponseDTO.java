package applesquare.moment.chat.dto;

import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomReadResponseDTO {
    private Long id;    // 채팅방 ID
    private List<UserProfileReadResponseDTO> memberProfiles;   // 멤버 프로필 목록 (본인 포함)
    private boolean notificationEnabled;    // 알림 수신 여부
}
