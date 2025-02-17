package applesquare.moment.chat.repository;

import applesquare.moment.chat.model.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface CustomChatRoomRepository {
    // 특정 사용자가 속한 채팅방 목록 검색 (정렬 기준 : 가장 최근에 대화한 곳부터)
    List<ChatRoom> search(String userId, String keyword, Long cursor, int size);
    // 멤버 ID를 기반으로 1:1 채팅방 조회
    Optional<ChatRoom> findPrivateChatRoomByMemberId(String myUserId, String otherUserId);
}
