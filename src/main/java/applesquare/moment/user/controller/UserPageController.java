package applesquare.moment.user.controller;

import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.user.dto.UserPageReadResponseDTO;
import applesquare.moment.user.service.UserPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}")
public class UserPageController {
    private final UserPageService userPageService;


    /**
     * 유저 페이지 조회 API
     * @param userId 사용자 ID
     * @return  (status) 200,
     *          (body)  유저 페이지 조회 성공 메세지,
     *                  유저 페이지
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> readUserPage(@PathVariable(name = "userId") String userId){
        // 유저 페이지 조회
        UserPageReadResponseDTO userPageReadResponseDTO=userPageService.readUserPageById(userId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "유저 페이지 조회에 성공했습니다.");
        responseMap.put("userPage", userPageReadResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
