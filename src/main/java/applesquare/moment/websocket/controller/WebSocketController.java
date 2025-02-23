package applesquare.moment.websocket.controller;

import applesquare.moment.chat.dto.ChatMessageCreateRequestDTO;
import applesquare.moment.chat.dto.ChatMessageReadResponseDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class WebSocketController {
    @MessageMapping("/chat/rooms/{roomId}")  // 수신기 역할
    @SendTo("/topic/chat/rooms/{roomId}")  // 발신기 역할
    public ChatMessageReadResponseDTO sendMessage(@DestinationVariable("roomId") Long roomId,
                                                  @Payload ChatMessageCreateRequestDTO message,
                                                  Principal principal) {
        // 사용자 ID 추출
        String userId = principal.getName();

        // TO DO : 메시지 전송 로직 수행

        return null;
    }
}