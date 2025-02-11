package applesquare.moment.notification.scheduler;

import applesquare.moment.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Log4j2
@RequiredArgsConstructor
public class NotificationCleanupScheduler {
    private final int RETENTION_DAYS = 14;  // 알림 보관 기간 : 14일
    private final NotificationService notificationService;


    @Scheduled(cron = "0 0 4 * * ?")
    public void scheduleCleanupTask() {
        log.info(String.format("알림 자동 삭제 작업 중... (time = %s)", LocalDateTime.now()));

        notificationService.deleteOldNotifications(RETENTION_DAYS)
                .thenAccept((deletedCount)->{
                    log.info(String.format("알림 %d건 자동 삭제 완료! (time = %s)", deletedCount, LocalDateTime.now()));
                });

    }
}
