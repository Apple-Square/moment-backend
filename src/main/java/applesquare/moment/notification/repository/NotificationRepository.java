package applesquare.moment.notification.repository;

import applesquare.moment.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Modifying
    @Query("DELETE FROM Notification  n " +
            "WHERE n.regDate < :cutoffDatetime")
    int deleteBeforeCutoffDatetime(@Param("cutoffDatetime") LocalDateTime cutoffDatetime);
}
