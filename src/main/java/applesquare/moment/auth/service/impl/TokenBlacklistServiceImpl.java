package applesquare.moment.auth.service.impl;

import applesquare.moment.auth.service.TokenBlacklistService;
import applesquare.moment.redis.model.RedisKeyType;
import applesquare.moment.redis.repository.RedisRepository;
import applesquare.moment.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;


@Service
@Transactional
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final RedisRepository redisRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void blacklist(String token, String reason){
        long remainingMilliSec=jwtUtil.getRemainingMilliSecFromToken(token);
        redisRepository.saveWithTimeout(RedisKeyType.BLACKLIST, token, reason, remainingMilliSec, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean exists(String token){
        return redisRepository.exists(RedisKeyType.BLACKLIST, token);
    }
}
