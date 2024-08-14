package applesquare.moment.like.controller;

import applesquare.moment.exception.ResponseMap;
import applesquare.moment.like.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/like")
public class PostLikeController {
    private final PostLikeService postLikeService;

    /**
     * 게시글 좋아요 API
     * @param postId 게시글 ID
     * @return  (status) 201,
     *          (body)  게시글 좋아요 성공 메세지,
     *                  좋아요를 누른 게시글 ID
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> like(@PathVariable Long postId){
        // 게시글 좋아요
        Long result=postLikeService.like(postId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "게시글 좋아요에 성공했습니다.");
        responseMap.put("id", result);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }


    /**
     * 게시글 좋아요 취소 API
     * @param postId 게시글 ID
     * @return  (status) 200,
     *          (body) 게시글 좋아요 취소 성공 메세지
     */
    @DeleteMapping("")
    public ResponseEntity<Map<String, Object>> unlike(@PathVariable Long postId){
        // 게시글 좋아요 취소
        postLikeService.unlike(postId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "게시글 좋아요 취소에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
