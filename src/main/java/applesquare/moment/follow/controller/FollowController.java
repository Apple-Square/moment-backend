package applesquare.moment.follow.controller;

import applesquare.moment.exception.ResponseMap;
import applesquare.moment.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{followeeId}/follow")
public class FollowController{
    private final FollowService followService;

    /**
     * 팔로우 API
     * @param followeeId 팔로우할 사용자 ID
     * @return  (status) 201,
     *          (body)  팔로우 성공 메세지,
     *                  팔로우 ID
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> follow(@PathVariable String followeeId){
        // 사용자 팔로우
        Long followId= followService.follow(followeeId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 팔로우에 성공했습니다.");
        responseMap.put("followId", followId);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 팔로우 취소 API
     * @param followeeId 팔로우 취소할 사용자 ID
     * @return  (status) 200,
     *          (body)  팔로우 취소 성공 메세지
     */
    @DeleteMapping("")
    public ResponseEntity<Map<String, Object>> unfollow(@PathVariable String followeeId){
        // 사용자 팔로우 취소
        followService.unfollow(followeeId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 팔로우 취소에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
