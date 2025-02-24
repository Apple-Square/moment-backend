package applesquare.moment.chat.controller;

import applesquare.moment.chat.dto.ChatRoomCreateRequestDTO;
import applesquare.moment.chat.dto.ChatRoomInviteRequestDTO;
import applesquare.moment.chat.dto.ChatRoomReadAllResponseDTO;
import applesquare.moment.chat.dto.ChatRoomReadResponseDTO;
import applesquare.moment.chat.service.ChatRoomService;
import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final SecurityService securityService;


    /**
     * 채팅방 목록 검색 API
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @param keyword 검색 키워드
     * @return  (status) 200,
     *          (body)  조회 성공 메시지,
     *                  채팅방 목록
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> search(@RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                       @RequestParam(value = "cursor", required = false) String cursor,
                                                       @RequestParam(value = "keyword", required = false) String keyword){
        // 권한 검사 : 로그인 상태
        String myUserId=securityService.getUserId();

        // 채팅방 목록 검색
        PageRequestDTO pageRequestDTO= PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .keyword(keyword)
                .build();
        PageResponseDTO<ChatRoomReadAllResponseDTO> pageResponseDTO=chatRoomService.search(myUserId, pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "채팅방 목록 검색에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 채팅방 생성 API
     * @param chatRoomCreateRequestDTO 채팅방 생성 요청 정보
     * @return  (status) 201,
     *          (body)  생성 성공 메시지,
     *                  채팅방 정보
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> createChatRoom(@Valid @RequestBody ChatRoomCreateRequestDTO chatRoomCreateRequestDTO){
        // 권한 검사 : 로그인 상태
        String myUserId=securityService.getUserId();

        // 채팅방 생성
        ChatRoomReadResponseDTO chatRoomDTO=chatRoomService.create(myUserId, chatRoomCreateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "채팅방 생성에 성공했습니다.");
        responseMap.put("chatRoom", chatRoomDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 1:1 채팅방 조회 API
     * @param otherUserId 상대 사용자 ID
     * @return  (status) 200,
     *          (body)  생성 성공 메시지,
     *                  채팅방 정보
     */
    @PostMapping("/private/{otherUserId}")
    public ResponseEntity<Map<String, Object>> createOrReadPrivateRoom(@PathVariable(name = "otherUserId") String otherUserId){
        // 권한 검사 : 로그인 상태
        String myUserId=securityService.getUserId();

        // 1:1 채팅방 조회
        ChatRoomReadResponseDTO chatRoomDTO=chatRoomService.readPrivateRoomByMemberId(myUserId, otherUserId);

        if(chatRoomDTO != null){
            // 이미 채팅방이 존재하는 경우, 조회 결과 반환
            ResponseMap responseMap=new ResponseMap();
            responseMap.put("message", "1:1 채팅방 조회에 성공했습니다.");
            responseMap.put("chatRoom", chatRoomDTO);

            return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
        }
        else{
            // 채팅방이 아직 존재하지 않는 경우, 1:1 채팅방 생성
            chatRoomDTO=chatRoomService.createPrivateRoom(myUserId, otherUserId);

            // 응답 생성
            ResponseMap responseMap=new ResponseMap();
            responseMap.put("message", "1:1 채팅방 생성에 성공했습니다.");
            responseMap.put("chatRoom", chatRoomDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
        }
    }

    /**
     * 채팅방 조회 API
     * @param roomId 채팅방 ID
     * @return  (status) 200,
     *          (body)  조회 성공 메시지,
     *                  채팅방 정보
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<Map<String, Object>> read(@PathVariable(name = "roomId") Long roomId){
        // 권한 검사 : 로그인 상태
        String myUserId=securityService.getUserId();

        // 채팅방 조회
        ChatRoomReadResponseDTO chatRoomDTO=chatRoomService.readById(myUserId, roomId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "채팅방 조회에 성공했습니다.");
        responseMap.put("chatRoom", chatRoomDTO);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 특정 채팅방의 멤버 목록 조회 API
     * @param roomId 채팅방 ID
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  조회 성공 메시지,
     *                  멤버 프로필 목록
     */
    @GetMapping("/{roodId}/members")
    public ResponseEntity<Map<String, Object>> readChatRoomMemberAll(@PathVariable(name = "roodId") Long roomId,
                                                                     @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                                     @RequestParam(value = "cursor", required = false) String cursor){
        // 권한 검사 : 로그인 상태
        String myUserId=securityService.getUserId();

        // 채팅방 멤버 목록 조회
        PageRequestDTO pageRequestDTO= PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .build();
        PageResponseDTO<UserProfileReadResponseDTO> pageResponseDTO=chatRoomService.readMemberProfileAllByRoomId(myUserId, roomId, pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "채팅방 멤버 목록 조회에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 채팅방 알림 수신 설정 API
     * @param roomId 채팅방 ID
     * @param enabled 알림 수신 여부
     * @return  (status) 200,
     *          (body) 설정 성공 메시지
     */
    @PatchMapping(value = "/{roomId}/notifications", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> setNotificationEnabled(@PathVariable(name = "roomId") Long roomId,
                                                                      @RequestBody Boolean enabled){
        // 사용자 ID 추출
        String myUserId=securityService.getUserId();

        // 채팅방 알림 활성화 여부 설정
        chatRoomService.setNotificationEnabled(myUserId, roomId, enabled);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "채팅방 알림 "+ (enabled? "활성화" : "비활성화") +"에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 채팅방 초대 API
     * @param roomId 채팅방 ID
     * @param chatRoomInviteRequestDTO 채팅방 초대 요청 정보
     * @return  (status) 201,
     *          (body)  초대 성공 메시지,
     *                  초대한 채팅방 ID,
     *                  초대한 멤버 프로필 목록
     */
    @PostMapping(value = "/{roomId}/invite", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> invite(@PathVariable(name = "roomId") Long roomId,
                                                      @Valid @RequestBody ChatRoomInviteRequestDTO chatRoomInviteRequestDTO){
        // 권한 검사 : 로그인 상태
        String myUserId=securityService.getUserId();

        // 채팅방으로 초대하기
        List<UserProfileReadResponseDTO> invitedMemberProfiles=chatRoomService.invite(myUserId, roomId, chatRoomInviteRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "채팅방 초대에 성공했습니다.");
        responseMap.put("roomId", roomId);
        responseMap.put("invitedMembers", invitedMemberProfiles);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 채팅방 나가기 API
     * @param roomId 채팅방 ID
     * @return  (status) 200,
     *          (body)  나가기 성공 메시지,
     *                  나간 채팅방 ID
     */
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable(name = "roomId") Long roomId){
        // 권한 검사 : 로그인 상태
        String myUserId=securityService.getUserId();

        // 채팅방 나가기
        chatRoomService.leave(myUserId, roomId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "채팅방 나가기에 성공했습니다.");
        responseMap.put("roomId", roomId);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
