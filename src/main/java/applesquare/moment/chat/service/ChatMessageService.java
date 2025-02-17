package applesquare.moment.chat.service;

import applesquare.moment.chat.dto.ChatMessageCreateRequestDTO;
import applesquare.moment.chat.dto.ChatMessageReadResponseDTO;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;

public interface ChatMessageService {
    // 특정 채팅방의 메시지 목록 조회
    PageResponseDTO<ChatMessageReadResponseDTO> readAll(String myUserId, Long roomId, PageRequestDTO pageRequestDTO);
    // 특정 채팅방에 메시지 생성 & 전송
    ChatMessageReadResponseDTO createAndSend(String myUserId, Long roomId, ChatMessageCreateRequestDTO chatMessageCreateRequestDTO);
    // 특정 메시지 소프트 삭제
    void setAsDelete(String myUserId, Long messageId);
    // 특정 메시지 읽기
    void setAsRead(String myUserId, Long messageId);

    // 채팅방 ID 기반으로 채팅 메시지의 파일 정보 일괄 삭제
    void deleteBatchByRoomId(Long roomId);
}
