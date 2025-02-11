package applesquare.moment.notification.repository;

import applesquare.moment.notification.model.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    // 수신자 ID에 따라 알림 목록 조회
    @EntityGraph(attributePaths = {"notification"})
    @Query("SELECT un " +
            "FROM UserNotification un " +
            "WHERE un.receiverId=:receiverId " +
                "AND (:cursor IS NULL OR un.id <: cursor)")
    List<UserNotification> findAllByReceiverId(@Param("receiverId") String receiverId,
                                               @Param("cursor") Long cursor,
                                               Pageable pageable);

    // lastEventId보다 이후에 생성된 알림 목록 조회
    @EntityGraph(attributePaths = {"notification"})
    @Query("SELECT un " +
            "FROM UserNotification un " +
            "WHERE un.receiverId=:receiverId " +
                "AND un.id>:lastEventId")
    List<UserNotification> findMissedNotificationByReceiverId(@Param("receiverId") String receiverId,
                                                              @Param("lastEventId") Long lastEventId);

    // 수신인 ID에 따라 미확인 알림 개수 조회
    @Query("SELECT count(un) " +
            "FROM UserNotification un " +
            "WHERE un.receiverId=:receiverId " +
                "AND un.isRead=false")
    long countUnreadNotificationsByReceiverId(@Param("receiverId") String receiverId);

    // 알림 ID에 따라 사용자 알림 개수 조회
    @Query("SELECT count(un) " +
            "FROM UserNotification un " +
            "WHERE un.notification.id=:notificationId")
    long countByNotificationId(@Param("notificationId") Long notificationId);

    // 특정 날짜 이전에 생성된 알림 삭제
    @Modifying
    @Query(value = "DELETE un FROM user_notification un " +
            "LEFT JOIN notification n ON un.notification_id = n.id " +
            "WHERE n.reg_date < :cutoffDate", nativeQuery = true)
    int deleteBeforeCutoffDate(@Param("cutoffDate") String cutoffDate);
}
