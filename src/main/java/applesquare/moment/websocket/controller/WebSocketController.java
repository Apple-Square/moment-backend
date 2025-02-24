package applesquare.moment.websocket.controller;

import applesquare.moment.chat.dto.ChatMessageCreateRequestDTO;
import applesquare.moment.chat.dto.ChatMessageReadResponseDTO;
import applesquare.moment.chat.service.ChatMemberActiveService;
import applesquare.moment.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class WebSocketController {
    private final ChatMessageService chatMessageService;
    private final ChatMemberActiveService chatMemberActiveService;


    /**
     * 채팅방 메시지 전송 엔드 포인트
     * @param roomId 채팅방 ID
     * @param chatMessageCreateRequestDTO 채팅 메시지 생성 요청 정보
     * @param principal 인증 정보
     * @return  메시지 브로커에게 메시지 전달
     *          (특정 채팅방 토픽을 구독한 사람들에게 브로드 캐스트 전송)
     */
    @MessageMapping("/chat/rooms/{roomId}")  // 수신기 역할
    @SendTo("/topic/chat/rooms/{roomId}")  // 발신기 역할
    public ChatMessageReadResponseDTO sendMessage(@DestinationVariable("roomId") Long roomId,
                                                  @Payload ChatMessageCreateRequestDTO chatMessageCreateRequestDTO,
                                                  Principal principal) {
        // 사용자 ID 추출
        String myUserId = principal.getName();

        // 채팅 메시지 생성 & 전송하기
        ChatMessageReadResponseDTO chatMessageDTO=chatMessageService.createAndSend(myUserId, roomId, chatMessageCreateRequestDTO);

        // 채팅 메시지를 '메시지 브로커'에 전달
        return chatMessageDTO;
    }

    /**
     * 채팅방 멤버 활성화 엔드 포인트
     * @param roomId 채팅방 ID
     * @param principal 인증 정보
     */
    @MessageMapping("/chat/rooms/{roomId}/active")
    public void setActive(@DestinationVariable("roomId") Long roomId,
                          Principal principal) {
        // 사용자 ID 추출
        String myUserId = principal.getName();

        // 채팅방 멤버 활성화
        chatMemberActiveService.activeMember(myUserId, roomId);
    }

    /**
     * 채팅방 멤버 비활성화 엔드 포인트
     * @param roomId 채팅방 ID
     * @param principal 인증 정보
     */
    @MessageMapping("/chat/rooms/{roomId}/inactive")
    public void setInactive(@DestinationVariable("roomId") Long roomId,
                            Principal principal) {
        // 사용자 ID 추출
        String myUserId = principal.getName();

        // 채팅방 멤버 비활성화
        chatMemberActiveService.inactiveMember(myUserId, roomId);
    }
}