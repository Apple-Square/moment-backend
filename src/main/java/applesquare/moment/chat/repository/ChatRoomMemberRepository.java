package applesquare.moment.chat.repository;

import applesquare.moment.chat.model.ChatRoomMember;
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
}
