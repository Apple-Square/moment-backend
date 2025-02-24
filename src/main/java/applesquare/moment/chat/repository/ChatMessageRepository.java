package applesquare.moment.chat.repository;

import applesquare.moment.chat.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @EntityGraph(attributePaths = {"sharedPost", "files"})
    @Query("SELECT cm " +
            "FROM ChatMessage cm " +
            "WHERE cm.chatRoom.id=:roomId " +
                "AND (:cursor IS NULL OR cm.id<:cursor)")
    List<ChatMessage> findChatMessageAllByRoomId(@Param("roomId") Long roomId,
                                                 @Param("cursor") Long cursor,
                                                 Pageable pageable);

    // (roomId)번 채팅방에서 (myUserId) 사용자가
    // (lastReadId)번 메시지 이후로 (messageId)번 메시지까지
    // 다른 사람이 작성한 메시지에 대해 안 읽은 인원 수(unreadCount)를 1씩 줄인다.
    @Modifying
    @Query("UPDATE ChatMessage cm " +
            "SET cm.unreadCount = cm.unreadCount - 1 " +
            "WHERE cm.chatRoom.id = :roomId " +
                "AND cm.senderId <> :myUserId " +
                "AND cm.id <= :messageId " +
                "AND cm.id > :lastReadId")
    void markMessagesAsRead(@Param("myUserId") String myUserId,
                            @Param("roomId") Long roomId,
                            @Param("messageId") Long messageId,
                            @Param("lastReadId") Long lastReadId);

    @Modifying
    @Query("UPDATE ChatMessage cm " +
            "SET cm.isDeleted = true " +
            "WHERE cm.id=:messageId")
    void markMessageAsDelete(@Param("messageId") Long messageId);

    void deleteByChatRoom_Id(Long chatRoomId);

    // 채팅방 ID 기반으로 해당 채팅방에서 생성(저장)된 파일명 조회
    @Query("SELECT f.filename " +
            "FROM ChatMessage cm " +
            "JOIN cm.files f " +
            "WHERE cm.chatRoom.id=:roomId")
    List<String> findFilenamesByChatRoomId(@Param("roomId") Long roomId, Pageable pageable);

    // 특정 사용자가 속한 모든 채팅방의 미확인 채팅 메시지 개수 조회
    @Query("SELECT COUNT(m) " +
            "FROM ChatRoomMember crm " +
                "JOIN ChatMessage m " +
            "WHERE crm.user.id = :userId " +
                "AND m.chatRoom = crm.chatRoom " +
                "AND (crm.lastReadMessageId IS NULL OR m.id > crm.lastReadMessageId)")
    long countUnreadMessagesByUserId(@Param("userId") String userId);
}
