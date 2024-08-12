package applesquare.moment.common.service.impl;

import applesquare.moment.auth.security.UserDetailsImpl;
import applesquare.moment.common.service.SecurityService;
import applesquare.moment.exception.TokenError;
import applesquare.moment.exception.TokenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class SecurityServiceImpl implements SecurityService {
    /**
     * 인증된 사용자의 ID를 추출
     * @return 사용자 ID
     * @throws TokenException 인증되지 않은 경우
     */
    @Override
    public String getUserId() throws TokenException{
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        if(authentication!=null){
            Object principal=authentication.getPrincipal();
            if(principal!=null && principal instanceof UserDetailsImpl){
                return ((UserDetailsImpl)principal).getId();
            }
        }

        throw new TokenException(TokenError.UNACCEPT);
    }
}
