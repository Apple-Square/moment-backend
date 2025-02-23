package applesquare.moment.chat.service;

public interface ChatMessageDeleteService {
    // 채팅방 ID 기반으로 채팅 메시지 일괄 삭제
    void deleteBatchByRoomId(Long roomId);
}
