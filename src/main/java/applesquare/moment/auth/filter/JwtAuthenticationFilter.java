package applesquare.moment.auth.filter;

import applesquare.moment.auth.security.UserDetailsImpl;
import applesquare.moment.auth.service.TokenBlacklistService;
import applesquare.moment.exception.TokenError;
import applesquare.moment.exception.TokenException;
import applesquare.moment.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Jwt Authentication Filter.........");

        try{
            // Authorization 헤더 추출
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            // 인증이 필요하지 않은 API도 있어서 Access Token이 없다고 예외를 던지면 안 됨.
            if (authorizationHeader != null) {
                // Bearer 토큰인지 검사
                if(authorizationHeader.startsWith("Bearer ")){
                    String accessToken = authorizationHeader.substring(7);

                    // 토큰 유효성 검사
                    if(jwtUtil.validateToken(accessToken)){
                        // 블랙 리스트에 등록되었는지 확인
                        if(!tokenBlacklistService.exists(accessToken)){
                            // Access 토큰의 Subject 값으로 UserDetails 조회
                            String userId=jwtUtil.getSubjectFromToken(accessToken);

                            // authentication 생성
                            UserDetails userDetails=new UserDetailsImpl(userId);
                            UsernamePasswordAuthenticationToken authentication= new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // ContextHolder에 authentication 등록
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                        else{
                            // 블랙 리스트에 등록되었다면 예외 처리
                            throw new TokenException(TokenError.BLACKLISTED);
                        }
                    }
                }
                else{
                    // Bearer 토큰이 아니면 예외 처리
                    throw new TokenException(TokenError.UNSUPPORTED);
                }
            }

            // 다음 필터로 이동
            filterChain.doFilter(request, response);
        }
        catch(TokenException e){
            log.error("TokenException: " + e.getMessage());
            e.sendErrorResponse(response);
        }
    }
}