package applesquare.moment.auth.service.impl;

import applesquare.moment.auth.repository.TokenBlacklistRepository;
import applesquare.moment.auth.service.TokenBlacklistService;
import applesquare.moment.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void blacklist(String token, String reason){
        long remainingMilliSec= jwtUtil.getRemainingMilliSecFromToken(token);
        tokenBlacklistRepository.saveStringWithTimeout(token, reason, remainingMilliSec, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean exists(String token){
        return tokenBlacklistRepository.exists(token);
    }
}
