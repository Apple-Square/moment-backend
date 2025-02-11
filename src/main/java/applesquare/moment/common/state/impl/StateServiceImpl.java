package applesquare.moment.common.state.impl;

import applesquare.moment.common.state.StateService;
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
    public void create(String state, String metaData, long ttl, TimeUnit timeUnit){
        redisRepository.saveWithTTL(RedisKeyType.STATE, state, metaData, ttl, timeUnit);
    }

    @Override
    public void delete(String state){
        redisRepository.delete(RedisKeyType.STATE, state);
    }

    @Override
    public String getMetaData(String state){
        Object value=redisRepository.get(RedisKeyType.STATE, state);
        if(value==null) return null;
        else return (String)value;
    }
}
