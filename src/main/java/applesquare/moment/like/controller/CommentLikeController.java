package applesquare.moment.like.controller;

import applesquare.moment.exception.ResponseMap;
import applesquare.moment.like.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments/{commentId}/like")
public class CommentLikeController {
    private final CommentLikeService commentLikeService;

    /**
     * 댓글 좋아요 API
     * @param commentId 댓글 ID
     * @return  (status) 201,
     *          (body)  댓글 좋아요 성공 메세지,
     *                  좋아요 누른 댓글 ID
     */
    @PostMapping("")
    public ResponseEntity<Map<String, Object>> like(@PathVariable Long commentId){
        // 댓글 좋아요
        Long result=commentLikeService.like(commentId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "댓글 좋아요에 성공했습니다.");
        responseMap.put("id", result);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 댓글 좋아요 취소 API
     * @param commentId 댓글 ID
     * @return  (status) 200
     *          (body) 댓글 좋아요 취소 성공 메세지
     */
    @DeleteMapping("")
    public ResponseEntity<Map<String, Object>> unlike(@PathVariable Long commentId){
        // 댓글 좋아요 취소
        commentLikeService.unlike(commentId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "댓글 좋아요 취소에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
