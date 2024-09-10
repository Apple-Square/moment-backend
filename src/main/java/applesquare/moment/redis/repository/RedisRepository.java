package applesquare.moment.redis.repository;

import applesquare.moment.redis.model.RedisKeyType;

import java.util.concurrent.TimeUnit;

public interface RedisRepository {
    void saveWithTimeout(RedisKeyType keyType, String key, String value, long timeout, TimeUnit unit);
    void delete(RedisKeyType keyType, String key);
    boolean exists(RedisKeyType keyType, String key);
}
