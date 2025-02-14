package applesquare.moment.follow.controller;

import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.follow.dto.FollowReadAllResponseDTO;
import applesquare.moment.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}")
public class FollowController{
    private final FollowService followService;

    /**
     * 팔로우 API
     * @param userId 팔로우할 사용자 ID
     * @return  (status) 201,
     *          (body)  팔로우 성공 메세지,
     *                  팔로우한 사용자 ID
     */
    @PostMapping("/follow")
    public ResponseEntity<Map<String, Object>> follow(@PathVariable(name = "userId") String userId){
        // 사용자 팔로우
        String result= followService.follow(userId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 팔로우에 성공했습니다.");
        responseMap.put("followeeId", result);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 팔로우 취소 API
     * @param userId 팔로우 취소할 사용자 ID
     * @return  (status) 200,
     *          (body)  팔로우 취소 성공 메세지,
     *                  팔로우 취소한 사용자 ID
     */
    @DeleteMapping("/follow")
    public ResponseEntity<Map<String, Object>> unfollow(@PathVariable(name = "userId") String userId){
        // 사용자 팔로우 취소
        String result=followService.unfollow(userId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 팔로우 취소에 성공했습니다.");
        responseMap.put("followeeId", result);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * (특정 사용자의) 팔로워 목록 조회 API
     * @param userId 사용자 ID
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  팔로워 목록 조회 성공 메세지,
     *                  팔로워 목록
     */
    @GetMapping("/followers/search")
    public ResponseEntity<Map<String, Object>> readFollowers(@PathVariable(name = "userId") String userId,
                                                             @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                             @RequestParam(value = "cursor", required = false) String cursor,
                                                             @RequestParam(value = "keyword", required = false) String keyword){
        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO= PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .keyword(keyword)
                .build();

        // 특정 유저의 팔로워 목록 조회
        PageResponseDTO<FollowReadAllResponseDTO> followerPage=followService.searchFollowerByKeyword(userId, pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "팔로워 검색에 성공했습니다.");
        responseMap.put("content", followerPage.getContent());
        responseMap.put("hasNext", followerPage.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * (특정 사용자의 ) 팔로잉 목록 조회 API
     * @param userId 사용자 ID
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  팔로잉 목록 조회 성공 메세지,
     *                  팔로잉 목록
     */
    @GetMapping("/followings/search")
    public ResponseEntity<Map<String, Object>> readFollowings(@PathVariable(name = "userId") String userId,
                                                              @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                              @RequestParam(value = "cursor", required = false) String cursor,
                                                              @RequestParam(value = "keyword", required = false) String keyword){
        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO= PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .keyword(keyword)
                .build();

        // 특정 유저의 팔로잉 목록 조회
        PageResponseDTO<FollowReadAllResponseDTO> followingPage=followService.searchFollowingByKeyword(userId, pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "팔로잉 검색에 성공했습니다.");
        responseMap.put("content", followingPage.getContent());
        responseMap.put("hasNext", followingPage.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
