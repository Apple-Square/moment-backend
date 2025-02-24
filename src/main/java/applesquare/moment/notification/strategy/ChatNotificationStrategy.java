package applesquare.moment.notification.strategy;

import applesquare.moment.chat.model.ChatMessage;
import applesquare.moment.chat.model.ChatRoomMember;
import applesquare.moment.chat.repository.ChatMessageRepository;
import applesquare.moment.chat.repository.ChatRoomMemberRepository;
import applesquare.moment.chat.service.ChatMemberActiveService;
import applesquare.moment.chat.service.ChatRoomService;
import applesquare.moment.notification.dto.NotificationReadResponseDTO;
import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.model.UserNotification;
import applesquare.moment.notification.service.NotificationSender;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatNotificationStrategy implements NotificationStrategy<ChatMessage>{
    private final NotificationSender notificationSender;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMemberActiveService chatMemberActiveService;
    private final UserProfileService userProfileService;


    @Override
    public NotificationType getSupportedType(){
        return NotificationType.CHAT;
    }

    /**
     * 특정 채팅방 멤버에게 채팅 알림 전송
     * @param notificationRequestDTO 알림 요청 정보
     */
    @Override
    public void saveAndsendNotification(NotificationRequestDTO<ChatMessage> notificationRequestDTO){
        // 채팅 알림은 알림 목록에서 조회할 일이 없기 때문에 DB에 저장하지 않습니다.

        // 채팅 송신자 정보 가져오기
        UserInfo sender=notificationRequestDTO.getSender();
        String senderId=sender.getId();
        UserProfileReadResponseDTO senderProfile=userProfileService.toUserProfileDTO(sender);

        // 채팅방, 채팅 메시지 가져오기
        Long chatRoomId=Long.parseLong(notificationRequestDTO.getReferenceId());
        ChatMessage chatMessage=notificationRequestDTO.getReferenceObject();

        // 채팅방 멤버 목록 조회
        Pageable pageable= PageRequest.of(0, ChatRoomService.MAX_MEMBER_COUNT);
        List<ChatRoomMember> chatRoomMembers=chatRoomMemberRepository.findAllByRoomId(senderId, chatRoomId, null, pageable);
        chatRoomMembers=chatRoomMembers.stream()
                .filter(chatRoomMember -> !chatRoomMember.getUser().getId().equals(senderId))
                .toList();

        // 채팅방의 각 멤버 별로 알림 전송
        for(ChatRoomMember chatRoomMember: chatRoomMembers){
            // 멤버의 사용자 ID 가져오기
            String memberUserId=chatRoomMember.getUser().getId();

            // 채팅방 알림 수신 여부 확인
            if(!chatRoomMember.isNotificationEnabled()){
                // 알림 수신이 꺼져있다면, 채팅 알림을 전송하지 않는다.
                continue;
            }

            // 채팅방 멤버 활성화 여부 확인
            if(chatMemberActiveService.isActiveMember(memberUserId, chatRoomId)){
                // 만약 해당 멤버가 채팅방 화면을 보고 있는 상태(=활성화 상태)라면,
                // 이 멤버한테는 채팅 알림을 전송하지 않는다.
                continue;
            }

            // 본인이 속한 채팅방의 전체에서 미확인 메시지 개수 총합 조회
            long badgeCount=chatMessageRepository.countUnreadMessagesByUserId(memberUserId);

            // 전달할 알림 DTO 생성
            String content=switch (chatMessage.getType()){
                case TEXT -> chatMessage.getContent();
                case IMAGE -> "사진을 보냈습니다.";
                case VIDEO -> "동영상을 보냈습니다.";
                case POST_SHARE -> "게시물을 공유했습니다.";
            };
            NotificationReadResponseDTO notificationDTO=NotificationReadResponseDTO.builder()
                    .sender(senderProfile)
                    .type(NotificationType.CHAT)
                    .title(senderProfile.getNickname()) // 제목 : 채팅 보낸 사람 이름
                    .content(content)  // 내용 : 채팅 메시지
                    .regDate(LocalDateTime.now())
                    .referenceId(notificationRequestDTO.getReferenceId())  // 래퍼런스 ID == 채팅방 ID
                    .build();

            // 배지 알림 전송
            notificationSender.sendChatBadge(memberUserId, badgeCount);

            // 팝업 알림 전송
            notificationSender.sendPopup(memberUserId, notificationDTO);
        }
    }

    @Override
    public void resendMissedSseNotification(UserNotification userNotification){
        // 배지 알림은 모든 손실된 알림에 대해서 1번만 전송하면 되므로 제외
        // 손실된 채팅 알림에 대한 내용은 채팅방 메시지 목록에서 확인할 수 있기 때문에 재전송이 불필요합니다.
    }
}
