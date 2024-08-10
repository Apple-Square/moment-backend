package applesquare.moment.auth.repository.impl;

import applesquare.moment.auth.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class TokenBlacklistRepositoryImpl implements TokenBlacklistRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public void saveStringWithTimeout(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public boolean exists(String key) {
        return stringRedisTemplate.hasKey(key);
    }
}
