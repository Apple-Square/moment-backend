package applesquare.moment.redis.repository.impl;

import applesquare.moment.redis.model.RedisKeyType;
import applesquare.moment.redis.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis에 타임 아웃 설정과 함께 키-값 추가
     * @param keyType 키 용도 (blacklist, state)
     * @param key 키
     * @param value 값
     * @param timeout 키-값의 수명
     * @param unit 타임 아웃 시간 단위
     */
    @Override
    public void saveWithTimeout(RedisKeyType keyType, String key, String value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(getKey(keyType, key), value, timeout, unit);
        }
        catch(RedisSystemException e){
            // 만료 시간이 너무 짧은 경우 발생
            // 아무 것도 저장하지 않고 나가면 되므로
            // 별도의 예외 처리를 하지 않음
        }
    }

    /**
     * 키를 기반으로 키-값 삭제
     * @param keyType 키 용도 (blacklist, state)
     * @param key 키
     */
    @Override
    public void delete(RedisKeyType keyType, String key){
        // Key 기반으로 데이터 삭제 (Redis는 존재하지 않는 Key를 삭제해도 예외 발생하지 않음)
        redisTemplate.delete(getKey(keyType, key));
    }

    /**
     * Redis에 특정 키가 존재하는지 확인
     * @param keyType 키 용도 (blacklist, state)
     * @param key 키
     * @return 키 존재 여부
     */
    @Override
    public boolean exists(RedisKeyType keyType, String key) {
        return redisTemplate.hasKey(getKey(keyType, key));
    }


    /**
     * Redis에 넣을 키 이름 생성
     * @param keyType 키 용도 (blacklist, state)
     * @param key 키
     * @return 키 이름
     */
    private String getKey(RedisKeyType keyType, String key){
        return keyType.name()+":"+key;
    }
}