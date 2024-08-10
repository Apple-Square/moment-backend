package applesquare.moment.auth.repository.impl;

import applesquare.moment.auth.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenBlacklistRepositoryImpl implements TokenBlacklistRepository {
    private final StringRedisTemplate stringRedisTemplate;

    public void saveStringWithTimeout(String key, String value, long timeout, TimeUnit unit) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
        }
        catch(RedisSystemException e){
            // 만료 시간이 너무 짧은 경우 발생
            // 아무 것도 저장하지 않고 나가면 되므로
            // 별도의 예외 처리를 하지 않음
        }
    }

    public boolean exists(String key) {
        return stringRedisTemplate.hasKey(key);
    }
}
