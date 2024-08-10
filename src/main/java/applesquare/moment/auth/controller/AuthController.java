package applesquare.moment.auth.controller;

import applesquare.moment.auth.dto.UserCreateRequestDTO;
import applesquare.moment.auth.service.AuthService;
import applesquare.moment.exception.ResponseMap;
import applesquare.moment.exception.TokenError;
import applesquare.moment.exception.TokenException;
import applesquare.moment.util.JwtUtil;
import applesquare.moment.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;


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

    /**
     * 토큰 재발급 API
     * (Refresh 토큰을 이용해서 새로운 Access 토큰 발급)
     *
     * Refresh 토큰이 만료되지 않았다면 Authorization 헤더에 Access 토큰을 추가하고,
     * Refresh 토큰의 만료 기한이 얼마 남지 않았다면 새로운 Refresh Cookie를 전달합니다.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    @PostMapping("/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try{
            // Refresh 토큰 추출
            String refreshToken= RequestUtil.getRefreshTokenFromRequest(request);

            // Refresh Token 이 없으면 토큰 재발급 불가
            if(refreshToken==null){
                throw new TokenException(TokenError.UNACCEPT);
            }

            // Refresh 토큰이 유효하다면
            if(jwtUtil.validateToken(refreshToken)){
                // 응답 구성
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpServletResponse.SC_OK);

                // Access Token 재발급
                String username = jwtUtil.getSubjectFromToken(refreshToken);
                String accessToken = jwtUtil.generateAccessToken(username);
                response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

                // Refresh 쿠키 유효 시간이 얼마 안 남았으면, Refresh 쿠키도 재발급
                if(jwtUtil.needNewRefreshToken(refreshToken)){
                    ResponseCookie newRefreshTokenCookie= jwtUtil.generateRefreshCookie(username);
                    response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());
                }

                // 응답 본문 구성
                ResponseMap responseMap=new ResponseMap();
                responseMap.put("message", "토큰 재발급에 성공했습니다.");

                response.getWriter().write(responseMap.toJson());
            }
        }
        catch(TokenException e){
            log.error("TokenException: "+e.getMessage());
            e.sendErrorResponse(response);
        }
    }
}
