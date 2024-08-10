package applesquare.moment.auth.filter;

import applesquare.moment.auth.service.AuthService;
import applesquare.moment.util.RequestUtil;
import applesquare.moment.util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.util.Map;

@Log4j2
public class LoginFilter extends AbstractAuthenticationProcessingFilter {
    public LoginFilter(String defaultFilterProcessesUrl){
        super(defaultFilterProcessesUrl);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("Login Filter.........");

        // GET 방식으로 호출하면 request 에서 NullPointerException 발생
        if(request.getMethod().equalsIgnoreCase("GET")){
            throw new BadCredentialsException("GET 메소드는 지원하지 않습니다.");
        }

        Map<String, String> requestBody= RequestUtil.parseRequestToMap(request);
        if(requestBody!=null){
            // 입력 형식 검사
            String username=requestBody.get("username");
            String password=requestBody.get("password");

            // null 검사
            if(username==null){
                throw new BadCredentialsException("아이디를 입력해주세요.");
            }
            if(password==null){
                throw new BadCredentialsException("비밀번호를 입력해주세요.");
            }

            // 길이 검사
            int usernameLength=username.length();
            int passwordLength=password.length();
            if(usernameLength < AuthService.MIN_USERNAME_LENGTH || usernameLength > AuthService.MAX_USERNAME_LENGTH){
                throw new BadCredentialsException("username: 크기가 " + AuthService.MIN_USERNAME_LENGTH + "에서 " + AuthService.MAX_USERNAME_LENGTH + " 사이여야 합니다.");
            }
            if(passwordLength < AuthService.MIN_PASSWORD_LENGTH || passwordLength > AuthService.MAX_PASSWORD_LENGTH){
                throw new BadCredentialsException("password: 크기가 " + AuthService.MIN_PASSWORD_LENGTH + "에서 " + AuthService.MAX_PASSWORD_LENGTH + " 사이여야 합니다.");
            }

            // 정규식 검사
            if(!username.matches(ValidationUtil.USERNAME_PATTERN)){
                throw new BadCredentialsException("username: 입력 형식이 올바르지 않습니다.");
            }
            if(!password.matches(ValidationUtil.PASSWORD_PATTERN)){
                throw new BadCredentialsException("password: 입력 형식이 올바르지 않습니다.");
            }

            // 로그인 작업
            UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication=getAuthenticationManager().authenticate(authenticationToken);

            // 인증 정보 반환
            return authentication;
        }
        else{
            throw new BadCredentialsException("아이디와 비밀번호를 입력해주세요.");
        }
    }
}