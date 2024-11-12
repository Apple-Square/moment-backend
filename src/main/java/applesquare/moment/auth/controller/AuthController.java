package applesquare.moment.auth.controller;

import applesquare.moment.auth.dto.EmailValidateRequestDTO;
import applesquare.moment.auth.dto.NicknameValidateRequestDTO;
import applesquare.moment.auth.dto.UserCreateRequestDTO;
import applesquare.moment.auth.dto.UsernameValidateRequestDTO;
import applesquare.moment.auth.service.AuthService;
import applesquare.moment.auth.service.TokenBlacklistService;
import applesquare.moment.email.service.EmailValidationService;
import applesquare.moment.exception.DuplicateDataException;
import applesquare.moment.exception.ResponseMap;
import applesquare.moment.exception.TokenError;
import applesquare.moment.exception.TokenException;
import applesquare.moment.user.service.UserInfoService;
import applesquare.moment.util.JwtUtil;
import applesquare.moment.util.RequestUtil;
import applesquare.moment.util.Validator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserInfoService userInfoService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;


    /**
     * 회원가입 API
     * @param userCreateRequestDTO 회원가입 정보
     * @return  (status) 201,
     *          (body) 회원가입 성공 메세지
     */
    @PostMapping(value="/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody UserCreateRequestDTO userCreateRequestDTO, HttpServletRequest request){
        String emailState=RequestUtil.getValueFromCookies(request.getCookies(), EmailValidationService.EMAIL_STATE_COOKIE);

        // 사용자 계정 생성
        authService.createUser(userCreateRequestDTO, emailState);

        // 응답 생성
        ResponseMap responseMap =new ResponseMap();
        responseMap.put("message", "회원가입에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 토큰 재발급 API
     * (Refresh 토큰을 이용해서 새로운 Access 토큰 발급)
     *
     * Refresh 토큰이 만료되지 않았다면 Authorization 헤더에 Access 토큰을 추가하고,
     * Refresh 토큰의 만료 기한이 얼마 남지 않았다면 새로운 Refresh 쿠키를 전달합니다.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @PostMapping("/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Refresh 토큰 추출
        String refreshToken= RequestUtil.getRefreshTokenFromRequest(request);

        // Refresh Token 이 없으면 토큰 재발급 불가
        if(refreshToken==null){
            throw new TokenException(TokenError.UNACCEPT);
        }

        // Refresh 토큰이 유효하다면
        if(jwtUtil.validateToken(refreshToken)){
            // 블랙 리스트에 등록되었는지 확인
            if(!tokenBlacklistService.exists(refreshToken)){
                // 응답 구성
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpServletResponse.SC_OK);

                // Access Token 재발급
                String userId = jwtUtil.getSubjectFromToken(refreshToken);
                String accessToken = jwtUtil.generateAccessToken(userId);
                response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

                // Refresh 쿠키 유효 시간이 얼마 안 남았으면, Refresh 쿠키도 재발급
                if(jwtUtil.needNewRefreshToken(refreshToken)){
                    ResponseCookie newRefreshTokenCookie= jwtUtil.generateRefreshCookie(userId);
                    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());
                }

                // 응답 본문 구성
                ResponseMap responseMap=new ResponseMap();
                responseMap.put("message", "토큰 재발급에 성공했습니다.");

                response.getWriter().write(responseMap.toJson());
            }
            else{
                throw new TokenException(TokenError.BLACKLISTED);
            }
        }
    }

    /**
     * 아이디 유효성 검사 API
     * @param username 아이디
     * @return  (status) 200,
     *          (body)  아이디 유효성 여부 메세지,
     *                  아이디 유효성 여부
     */
    @GetMapping("/username/validation")
    public ResponseEntity<Map<String, Object>> validateUsername(@RequestParam(value = "username", required = true) String username){
        // DTO 생성
        UsernameValidateRequestDTO usernameValidateRequestDTO= UsernameValidateRequestDTO.builder()
                .username(username)
                .build();

        // Validation 검사
        Validator.validate(usernameValidateRequestDTO);

        // 아이디 유일성 검사
        boolean isUnique= authService.isUniqueUsername(username);
        if(!isUnique){
            throw new DuplicateDataException("이미 존재하는 아이디입니다. (id = "+username+")");
        }

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용 가능한 아이디입니다.");
        responseMap.put("available", true);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 이메일 유효성 검사 API
     * @param email 이메일
     * @return  (status) 200,
     *          (body)  이메일 유효성 여부 메세지,
     *                  이메일 유효성 여부
     */
    @GetMapping("/email/validation")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam(value = "email", required = true) String email){
        // DTO 생성
        EmailValidateRequestDTO emailValidateRequestDTO=EmailValidateRequestDTO.builder()
                .email(email)
                .build();

        // Validation 검사
        Validator.validate(emailValidateRequestDTO);

        // 이메일 유일성 검사
        boolean isUnique=authService.isUniqueEmail(email);
        if(!isUnique){
            throw new DuplicateDataException("이미 사용 중인 이메일입니다. (email = "+email+")");
        }

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용 가능한 이메일입니다.");
        responseMap.put("available", true);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 닉네임 유효성 검사 API
     * @param nickname 닉네임
     * @return  (status) 200,
     *          (body)  닉네임 유효성 여부 메세지,
     *                  닉네임 유효성 여부
     */
    @GetMapping("/nickname/validation")
    public ResponseEntity<Map<String, Object>> validateNickname(@RequestParam(value = "nickname", required = true) String nickname){
        // DTO 생성
        NicknameValidateRequestDTO nicknameValidateRequestDTO=NicknameValidateRequestDTO.builder()
                .nickname(nickname)
                .build();

        // Validation 검사
        Validator.validate(nicknameValidateRequestDTO);

        // 닉네임 유일성 검사
        boolean isUnique=userInfoService.isUniqueNickname(nickname);
        if(!isUnique){
            throw new DuplicateDataException("이미 존재하는 닉네임입니다. (nickname = "+nickname+")");
        }

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용 가능한 닉네임입니다.");
        responseMap.put("available", true);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
