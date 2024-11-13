package applesquare.moment.email.controller;

import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.common.service.StateService;
import applesquare.moment.email.dto.EmailCodeRequestDTO;
import applesquare.moment.email.dto.EmailValidateRequestDTO;
import applesquare.moment.email.service.EmailValidationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email")
public class EmailValidationController {
    private final EmailValidationService emailValidationService;
    private final StateService stateService;


    /**
     * 이메일 인증 코드 발급 API
     * @param emailCodeRequestDTO 이메일 인증 코드 요청 정보
     * @return     (status) 200,
     *                      (body) 발급 성공 메세지
     */
    @PostMapping(value = "/code", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getCode(@Valid @RequestBody EmailCodeRequestDTO emailCodeRequestDTO){
        // 이메일 인증 코드 발급하기
        emailValidationService.storeAndSendEmailCode(emailCodeRequestDTO.getEmail());

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "이메일로 인증 코드를 전송했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 이메일 유효성 검증 API (인증 코드 검증)
     *
     * @param emailValidateRequestDTO 이메일 유효성 검사 요청 정보
     * @return  (status) 200,
     *                    (body) 이메일 유효성 여부
     */
    @PostMapping(value = "/code/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void validateCode(@Valid @RequestBody EmailValidateRequestDTO emailValidateRequestDTO, HttpServletResponse response) throws IOException {
        String email=emailValidateRequestDTO.getEmail();
        String code=emailValidateRequestDTO.getCode();

        // 이메일에 대해 인증 코드 검증하기
        // 인증 코드가 유효하지 않을 경우, RuntimeException 던짐
        emailValidationService.validateEmailCode(email, code);

        // 서버에서 인증 코드 제거
        emailValidationService.removeEmailCode(email);

        // 서버에 이메일 인증이 완료되었다는 state 생성
        String emailState= UUID.randomUUID().toString();
        stateService.create(emailState, email, EmailValidationService.EMAIL_STATE_TTL_MINUTE, TimeUnit.MINUTES);

        // 응답 생성
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

        // emailState를 쿠키에 실어서 보내기
        ResponseCookie emailStateCookie=ResponseCookie.from(EmailValidationService.EMAIL_STATE_COOKIE, emailState)
                .path("/")
                .maxAge(EmailValidationService.EMAIL_STATE_COOKIE_MAX_AGE)
                .httpOnly(true)
                .sameSite(Cookie.SameSite.STRICT.attributeValue())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, emailStateCookie.toString());

        // 응답 본문 구성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "이메일 인증에 성공했습니다.");

        response.getWriter().write(responseMap.toJson());
    }
}
