package applesquare.moment.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomReadAllResponseDTO {
    private Long id;    // 채팅방 ID
    private long memberCount;  // 멤버 인원 수
    private List<String> memberProfileImageUrls;   // 멤버 프로필 이미지 URL 목록 (최대 4개까지만)
    private ChatRoomMessageThumbnailDTO lastMessage;  // 가장 최근 메시지
}
