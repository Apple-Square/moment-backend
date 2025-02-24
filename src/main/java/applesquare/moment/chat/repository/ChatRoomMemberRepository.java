package applesquare.moment.chat.repository;

import applesquare.moment.chat.model.ChatRoomMember;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    long countByChatRoom_Id(Long chatRoomId);
    boolean existsByChatRoom_IdAndUser_Id(Long chatRoomId, String userId);
    Optional<ChatRoomMember> findByChatRoom_IdAndUser_Id(Long chatRoomId, String userId);
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT cm " +
            "FROM ChatRoomMember cm " +
            "WHERE cm.chatRoom.id = :roomId " +
                "AND cm.user.id <> :myUserId " +
                "AND (:cursor IS NULL OR cm.id > :cursor)")
    List<ChatRoomMember> findAllByRoomId(@Param("myUserId") String myUserId,
                                         @Param("roomId") Long roomId,
                                         @Param("cursor") Long cursor,
                                         Pageable pageable);

    // 내가 속해있는 특정 채팅방 목록에서 알림 수신 여부 조회
    @Query("SELECT cm.chatRoom.id AS chatRoomId, " +
                "cm.notificationEnabled AS enabled " +
            "FROM ChatRoomMember cm " +
            "WHERE cm.chatRoom.id IN :roomIds " +
                "AND cm.user.id = :userId")
    List<Tuple> findNotificationEnabledByChatRoomIdsAndUserId(@Param("userId") String userId,
                                                              @Param("roomIds") List<Long> roomIds);
}
