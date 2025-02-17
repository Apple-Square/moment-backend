package applesquare.moment.chat.controller;

import applesquare.moment.chat.dto.ChatMessageCreateRequestDTO;
import applesquare.moment.chat.dto.ChatMessageReadResponseDTO;
import applesquare.moment.chat.service.ChatMessageService;
import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.common.security.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final SecurityService securityService;


    /**
     * 특정 채팅방의 메시지 목록 조회하는 API
     * @param roomId 채팅방 ID
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  조회 성공 메시지,
     *                  메시지 목록
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Map<String, Object>> readAll(@PathVariable(name = "roomId") Long roomId,
                                                       @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                       @RequestParam(value = "cursor", required = false) String cursor){
        // 사용자 ID 추출
        String myUserId=securityService.getUserId();

        // 특정 채팅방의 메시지 목록 조회
        PageRequestDTO pageRequestDTO=PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .build();
        PageResponseDTO<ChatMessageReadResponseDTO> pageResponseDTO=chatMessageService.readAll(myUserId, roomId, pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "메시지 목록 조회에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 특정 채팅방에 메시지 전송하는 API
     * @param chatMessageCreateRequestDTO 메시지 생성 요청 정보
     * @return  (status) 201,
     *          (body)  전송 성공 메시지,
     *                  전송한 메시지 정보
     */
    @PostMapping(value = "/rooms/{roomId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> create(@PathVariable(name = "roomId") Long roomId,
                                                      @Valid @RequestBody ChatMessageCreateRequestDTO chatMessageCreateRequestDTO){
        // 사용자 ID 추출
        String myUserId=securityService.getUserId();

        // 채팅 메시지 생성 & 전송
        ChatMessageReadResponseDTO chatMessageDTO=chatMessageService.createAndSend(myUserId, roomId, chatMessageCreateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "메시지 전송에 성공했습니다.");
        responseMap.put("chatMessage", chatMessageDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 채팅 메시지 삭제 API
     * @param messageId 메시지 ID
     * @return  (status) 200,
     *          (body) 삭제 성공 메시지
     */
    @PatchMapping("/messages/{messageId}/delete")
    public ResponseEntity<Map<String, Object>> softDelete(@PathVariable(name = "messageId") Long messageId){
        // 사용자 ID 추출
        String myUserId=securityService.getUserId();

        // 메시지 소프트 삭제
        chatMessageService.setAsDelete(myUserId, messageId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "메시지 삭제에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 채팅 메시지 읽기 API
     * @param messageId 메시지 ID
     * @return  (status) 200,
     *          (body) 읽기 성공 메시지
     */
    @PatchMapping("/messages/{messageId}/view")
    public ResponseEntity<Map<String, Object>> view(@PathVariable(name = "messageId") Long messageId){
        // 사용자 ID 추출
        String myUserId=securityService.getUserId();

        // 메시지 읽음 처리
        chatMessageService.setAsRead(myUserId, messageId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "메시지 읽기에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
