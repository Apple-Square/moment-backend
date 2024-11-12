package applesquare.moment.common.service.impl;

import applesquare.moment.common.service.StateService;
import applesquare.moment.redis.model.RedisKeyType;
import applesquare.moment.redis.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StateServiceImpl implements StateService {
    private final RedisRepository redisRepository;

    @Override
    public void create(String state, long ttl, TimeUnit timeUnit){
        redisRepository.saveWithTTL(RedisKeyType.STATE, state, "", ttl, timeUnit);
    }

    @Override
    public void delete(String state){
        redisRepository.delete(RedisKeyType.STATE, state);
    }

    @Override
    public boolean exists(String state){
        return redisRepository.exists(RedisKeyType.STATE, state);
    }
}
