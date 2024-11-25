package applesquare.moment.redis.repository;

import applesquare.moment.redis.model.RedisKeyType;

import java.util.concurrent.TimeUnit;

public interface RedisRepository {
    void saveWithTTL(RedisKeyType keyType, String key, String value, long ttl, TimeUnit unit);
    boolean exists(RedisKeyType keyType, String key);
    Object get(RedisKeyType keyType, String key);
    void delete(RedisKeyType keyType, String key);
    boolean extendTTL(RedisKeyType keyType, String key, long ttl, TimeUnit unit);
}
