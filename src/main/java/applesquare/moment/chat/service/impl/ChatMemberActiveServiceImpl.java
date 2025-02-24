package applesquare.moment.chat.service.impl;

import applesquare.moment.chat.service.ChatMemberActiveService;
import applesquare.moment.chat.service.ChatRoomService;
import applesquare.moment.redis.model.RedisKeyType;
import applesquare.moment.redis.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMemberActiveServiceImpl implements ChatMemberActiveService {
    private final ChatRoomService chatRoomService;
    private final RedisRepository redisRepository;


    @Override
    public void activeMember(String userId, Long chatRoomId){
        // 권한 검사 : 채팅방 멤버인지 검사
        if(!chatRoomService.isMember(chatRoomId, userId)){
            throw new AccessDeniedException("채팅방 멤버만 활성화 여부를 기록할 수 있습니다.");
        }

        String key=getKey(userId, chatRoomId);
        if(redisRepository.exists(RedisKeyType.CHAT_MEMBER_ACTIVE, key)){
            // 이미 존재한다면 TTL 갱신
            redisRepository.setTTL(RedisKeyType.CHAT_MEMBER_ACTIVE, key, ACTIVE_TTL_SEC, TimeUnit.SECONDS);
        }
        else{
            redisRepository.saveWithTTL(RedisKeyType.CHAT_MEMBER_ACTIVE, key, "", ACTIVE_TTL_SEC, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean isActiveMember(String userId, Long chatRoomId){
        return redisRepository.exists(RedisKeyType.CHAT_MEMBER_ACTIVE, getKey(userId, chatRoomId));
    }

    @Override
    public void inactiveMember(String userId, Long chatRoomId){
        redisRepository.delete(RedisKeyType.CHAT_MEMBER_ACTIVE, getKey(userId, chatRoomId));
    }


    private String getKey(String userId, Long chatRoomId){
        return userId+":"+chatRoomId;
    }
}
