package applesquare.moment.notification.service.impl;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.notification.dto.NotificationLink;
import applesquare.moment.notification.dto.NotificationLinkType;
import applesquare.moment.notification.dto.NotificationReadResponseDTO;
import applesquare.moment.notification.model.Notification;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.model.UserNotification;
import applesquare.moment.notification.repository.NotificationRepository;
import applesquare.moment.notification.repository.UserNotificationRepository;
import applesquare.moment.notification.service.NotificationService;
import applesquare.moment.post.service.PostReadService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserProfileService userProfileService;
    private final PostReadService postReadService;
    private final SecurityService securityService;
    private final ModelMapper modelMapper;


    /**
     * UserNotification -> NotificationReadResponseDTO
     * @param userNotification UserNotification
     * @return NotificationReadResponseDTO
     */
    @Override
    public NotificationReadResponseDTO toNotificationReadResponseDTO(UserNotification userNotification){
        Notification notification= userNotification.getNotification();
        UserInfo sender=notification.getSender();

        // 송신자 프로필 구성
        UserProfileReadResponseDTO senderProfileDTO=userProfileService.toUserProfileDTO(sender);

        // 알림과 연관된 링크 가져오기
        List<NotificationLink> notificationLinks=getNotificationLinks(notification);

        // 알림 DTO 구성
        NotificationReadResponseDTO notificationDTO=modelMapper.map(notification, NotificationReadResponseDTO.class);

        return notificationDTO.toBuilder()
                .id(userNotification.getId())  // UserNotification 엔티티의 ID와 일치
                .sender(senderProfileDTO)
                .isRead(userNotification.isRead())
                .links(notificationLinks)
                .build();
    }

    /**
     * 특정 유저의 알림 목록 조회
     * (receiverId == 사용자 ID와 일치하는 목록)
     *
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 알림 목록
     */
    @Override
    public PageResponseDTO<NotificationReadResponseDTO> readAll(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 알림 목록 조회
        List<UserNotification> userNotifications=userNotificationRepository.findAllByReceiverId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(userNotifications.size()>pageRequestDTO.getSize()){
            userNotifications.remove(userNotifications.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<NotificationReadResponseDTO> notificationDTOs=userNotifications.stream()
                .map((userNotification)->toNotificationReadResponseDTO(userNotification))
                .toList();

        // 알림 페이지 반환
        return PageResponseDTO.<NotificationReadResponseDTO>builder()
                .content(notificationDTOs)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 특정 알림을 읽음 상태(isRead=true)로 변경
     * @param userNotificationId 사용자 알림 ID
     */
    @Override
    public void setAsRead(Long userNotificationId){
        // 권한 검사
        String userId= securityService.getUserId();

        // 본인이 받은 알림만 읽기 가능
        UserNotification userNotification=userNotificationRepository.findById(userNotificationId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자 알림입니다. (id = "+userNotificationId+")"));
        if(!isOwner(userNotification, userId)){
            throw new AccessDeniedException("알림 소유자만 읽을 수 있습니다.");
        }

        // 알림 읽음 처리
        UserNotification newUserNotification=userNotification.toBuilder()
                .isRead(true)
                .build();

        // UserNotification 엔티티 DB 저장
        userNotificationRepository.save(newUserNotification);
    }

    /**
     * 미확인 알림 개수 조회
     * @param userId 사용자 ID
     * @return 미확인 알림 개수
     */
    @Override
    public long countUnreadNotifications(String userId){
        return userNotificationRepository.countUnreadNotificationsByReceiverId(userId);
    }

    /**
     * 알림 유형(type)에 따라 필요한 NotificationLink 목록 생성
     * @param notification 알림
     * @return NotificationLink 목록
     */
    private List<NotificationLink> getNotificationLinks(Notification notification){
        NotificationType type=notification.getType();

        // 알림 유형(type)에 따라 Notification 목록 생성
        switch (type){
            case COMMENT:
            case POST_LIKE:
            case COMMENT_LIKE:
            case FOLLOW:
                return List.of();
            case FEED:
                // 게시물 썸네일 링크 생성
                String postThumbnailUrl=postReadService.readThumbnailFileUrl(notification.getReferenceIdAsLong());
                NotificationLink feedThumbnailLink=NotificationLink.builder()
                        .type(NotificationLinkType.THUMBNAIL)
                        .link(postThumbnailUrl)
                        .build();
                // NotificationLink 목록 반환
                return List.of(feedThumbnailLink);
            default:
                throw new RuntimeException("잘못된 알림 유형입니다. (type = "+notification.getType().name()+")");
        }
    }

    /**
     * 특정 사용자 알림 삭제
     * @param userNotificationId 사용자 알림 ID
     */
    @Override
    public void delete(Long userNotificationId){
        // 권한 검사
        String userId= securityService.getUserId();

        // 본인이 받은 알림만 삭제 가능
        UserNotification userNotification=userNotificationRepository.findById(userNotificationId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자 알림입니다. (id = "+userNotificationId+")"));
        if(!isOwner(userNotification, userId)){
            throw new AccessDeniedException("알림 소유자만 삭제할 수 있습니다.");
        }

        // Notification을 참조하는 UserNotification 개수 세기
        Long notificationId=userNotification.getNotification().getId();
        long referencedCount=userNotificationRepository.countByNotificationId(notificationId);

        if(referencedCount == 1){
            // Notification이 지금 들어온 사용자 알림에 의해서만 참조된다면 알림 자체를 삭제
            notificationRepository.deleteById(notificationId);
        } else if(referencedCount > 1){
            // Notification이 여러 사용자 알림에 의해 참조된다면 알림 자체는 유지하고, 특정 사용자 알림만 삭제
            userNotificationRepository.deleteById(userNotificationId);
        }
    }

    /**
     * SSE 재연결동안에 손실된 알림 조회
     * @param receiverId 사용자 ID
     * @param lastEventId 클라이언트가 가장 최근에 받은 사용자 알림 ID
     * @return 손실된 사용자 알림 목록
     */
    @Override
    public List<UserNotification> readMissedAll(String receiverId, Long lastEventId){
        List<UserNotification> missedNotifications=userNotificationRepository.findMissedNotificationByReceiverId(receiverId, lastEventId);
        return missedNotifications;
    }

    /**
     * 알림 보관 기간이 지난 오래된 알림 삭제
     * @param retentionDays 알림 보관 기간 (days)
     * @return 삭제된 알림 개수
     */
    @Async("taskExecutor")
    @Override
    public CompletableFuture<Long> deleteOldNotifications(int retentionDays){
        LocalDateTime cutoffDatetime=LocalDateTime.now().minusDays(retentionDays);
        String cutoffDateTimeStr=cutoffDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 기준일보다 이전에 생성된 UserNotification 삭제 (자식 엔티티)
        userNotificationRepository.deleteBeforeCutoffDate(cutoffDateTimeStr);

        // 기준일보다 이전에 생성된 Notificaiton 삭제 (부모 엔티티)
        int deletedCount=notificationRepository.deleteBeforeCutoffDatetime(cutoffDatetime);

        // 삭제한 알림 개수 반환
        return CompletableFuture.completedFuture((long) deletedCount);
    }

    /**
     * 특정 사용자가 특정 사용자 알림을 소유하고 있는지 검사
     * @param userNotification 사용자 알림
     * @param userId 사용자 ID
     */
    private boolean isOwner(UserNotification userNotification, String userId){
        return userNotification.getReceiverId().equals(userId);
    }
}