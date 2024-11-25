package applesquare.moment.user.controller;

import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.user.dto.UserInfoUpdateRequestDTO;
import applesquare.moment.user.service.UserInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserInfoController {
    private final UserInfoService userInfoService;

    
    /**
     * 사용자 정보 수정 API
     * @param userInfoUpdateRequestDTO 수정된 사용자 정보 입력
     * @return  (status) 200,
     *          (body)  수정 성공 메세지,
     *                  수정된 사용자의 ID
     */
    @PatchMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateUserInfo(@PathVariable String userId,
                                                              @Valid @RequestBody UserInfoUpdateRequestDTO userInfoUpdateRequestDTO){
        // 사용자 정보 수정
        String result=userInfoService.updateUserInfo(userId, userInfoUpdateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 정보 수정에 성공했습니다.");
        responseMap.put("userId", result);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
