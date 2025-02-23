package applesquare.moment.websocket.controller;

import applesquare.moment.chat.dto.ChatMessageCreateRequestDTO;
import applesquare.moment.chat.dto.ChatMessageReadResponseDTO;
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
}