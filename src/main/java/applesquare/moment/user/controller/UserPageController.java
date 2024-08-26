package applesquare.moment.user.controller;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.exception.ResponseMap;
import applesquare.moment.follow.service.FollowService;
import applesquare.moment.user.dto.UserPageReadResponseDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
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
    private final FollowService followService;


    /**
     * 유저 페이지 조회 API
     * @param userId 사용자 ID
     * @return  (status) 200,
     *          (body)  유저 페이지 조회 성공 메세지,
     *                  유저 페이지
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> readUserPage(@PathVariable String userId){
        // 유저 페이지 조회
        UserPageReadResponseDTO userPageReadResponseDTO=userPageService.readUserPageById(userId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "유저 페이지 조회에 성공했습니다.");
        responseMap.put("userPage", userPageReadResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * (특정 사용자의) 팔로워 목록 조회 API
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return  (status) 200,
     *          (body)  팔로워 목록 조회 성공 메세지,
     *                  팔로워 목록
     */
    @GetMapping("/followers")
    public ResponseEntity<Map<String, Object>> readFollowers(@PathVariable String userId, PageRequestDTO pageRequestDTO){
        // 특정 유저의 팔로워 목록 조회
        PageResponseDTO<UserProfileReadResponseDTO> followerPage=followService.readFollowerPage(userId, pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "팔로워 목록 조회에 성공했습니다.");
        responseMap.put("content", followerPage.getContent());
        responseMap.put("hasNext", followerPage.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * (특정 사용자의 ) 팔로잉 목록 조회 API
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return  (status) 200,
     *          (body)  팔로잉 목록 조회 성공 메세지,
     *                  팔로잉 목록
     */
    @GetMapping("/followings")
    public ResponseEntity<Map<String, Object>> readFollowings(@PathVariable String userId, PageRequestDTO pageRequestDTO){
        // 특정 유저의 팔로잉 목록 조회
        PageResponseDTO<UserProfileReadResponseDTO> followingPage=followService.readFollowingPage(userId, pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "팔로잉 목록 조회에 성공했습니다.");
        responseMap.put("content", followingPage.getContent());
        responseMap.put("hasNext", followingPage.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
