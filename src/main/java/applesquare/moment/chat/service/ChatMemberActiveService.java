package applesquare.moment.chat.service;

public interface ChatMemberActiveService {
    int ACTIVE_TTL_SEC=60;  // 60초

    void activeMember(String userId, Long chatRoomId);
    boolean isActiveMember(String userId, Long chatRoomId);
    void inactiveMember(String userId, Long chatRoomId);
}
