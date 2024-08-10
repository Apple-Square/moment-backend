package applesquare.moment.auth.controller;

import applesquare.moment.auth.dto.UserCreateRequestDTO;
import applesquare.moment.auth.service.AuthService;
import applesquare.moment.exception.ResponseMap;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * 회원가입 API
     * @param userCreateRequestDTO 회원가입 정보
     * @return  (status) 201,
     *          (body) 회원가입 성공 메세지
     */
    @PostMapping(value="/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody UserCreateRequestDTO userCreateRequestDTO){
        // 사용자 계정 생성
        authService.createUser(userCreateRequestDTO);

        // 응답 생성
        ResponseMap responseMap =new ResponseMap();
        responseMap.put("message", "회원가입에 성공했습니다.");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMap.getMap());
    }
}
