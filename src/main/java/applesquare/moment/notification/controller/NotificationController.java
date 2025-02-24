package applesquare.moment.notification.controller;

import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.notification.dto.NotificationReadResponseDTO;
import applesquare.moment.notification.service.NotificationSendService;
import applesquare.moment.notification.service.NotificationService;
import applesquare.moment.sse.service.SseCategory;
import applesquare.moment.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final SecurityService securityService;
    private final SseService sseService;
    private final NotificationSendService notificationSendService;


    /**
     * 알림에 대한 SSE 연결 생성
     * @return (status) 200,
     *         (body) SseEmitter
     */
    @GetMapping(value = "/subscribe",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@RequestHeader(value = "Last-Event-ID", required = false) String lastEventId){
        // 권한 검사
        String userId = securityService.getUserId();

        // SSE 연결
        SseEmitter emitter=sseService.connect(SseCategory.NOTIFICATION, userId);

        // SSE 연결이 끊어지고 다시 연결하는 사이에 발생한 알림 재전송
        if(lastEventId!=null){
            notificationSendService.resendMissedNotifications(userId, Long.parseLong(lastEventId));
        }

        return ResponseEntity.status(HttpStatus.OK).body(emitter);
    }

    /**
     * 나의 알림 목록 조회
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body) 조회 성공 메시지,
     *                  알림 목록
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> readAll(@RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                       @RequestParam(value = "cursor", required = false) String cursor){
        // 권한 검사 (로그인 상태 확인)
        String myUserId= securityService.getUserId();

        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO=PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .build();

        // 알림 목록 조회
        PageResponseDTO<NotificationReadResponseDTO> pageResponseDTO=notificationService.readAll(myUserId, pageRequestDTO);

        // 응답 객체 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "알림 목록 조회에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 특정 알림 읽음 처리
     * @param userNotificationId 알림 ID
     * @return (status) 200,
     *          (body) 알림 읽기 성공 메시지
     */
    @PatchMapping("/{userNotificationId}/view")
    public ResponseEntity<Map<String, Object>> view(@PathVariable(name = "userNotificationId") Long userNotificationId){
        // 알림 읽음 처리
        notificationService.setAsRead(userNotificationId);

        // 응답 객체 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "알림 읽기에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 특정 알림 삭제
     * @param userNotificationId 알림 ID
     * @return (status) 200,
     *          (body) 알림 삭제 성공 메시지
     */
    @DeleteMapping("/{userNotificationId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable(name = "userNotificationId") Long userNotificationId){
        // 알림 삭제
        notificationService.delete(userNotificationId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "알림 삭제에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 미확인 알림 개수 조회 API
     * @return  (status) 200,
     *          (body)  조회 성공 메시지,
     *                  미확인 알림 개수
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> readUnreadCount(){
        // 사용자 ID 추출
        String userId=securityService.getUserId();

        // 미확인 알림 개수 조회
        long unreadCount=notificationService.countUnreadNotifications(userId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "미확인 알림 개수 조회에 성공했습니다.");
        responseMap.put("unreadCount", unreadCount);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
