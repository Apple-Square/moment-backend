package applesquare.moment.comment.controller;

import applesquare.moment.comment.dto.CommentUpdateRequestDTO;
import applesquare.moment.comment.service.CommentService;
import applesquare.moment.common.exception.ResponseMap;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments/{commentId}")
public class CommentController {
    private final CommentService commentService;


    /**
     * 댓글 수정 API
     * @param commentId 댓글 ID
     * @param commentUpdateRequestDTO 댓글 변경 정보
     * @return  (status) 200,
     *          (body)  댓글 수정 성공 메세지,
     *                  수정된 댓글 ID
     */
    @PatchMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> update(@PathVariable(name = "commentId") Long commentId,
                                                      @Valid @RequestBody CommentUpdateRequestDTO commentUpdateRequestDTO){
        // 댓글 수정
        Long result=commentService.update(commentId, commentUpdateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "댓글 수정에 성공했습니다.");
        responseMap.put("id", result);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 댓글 삭제 API
     * @param commentId 댓글 ID
     * @return  (status) 200,
     *          (body)  댓글 삭제 성공 메세지
     */
    @DeleteMapping("")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable(name = "commentId") Long commentId){
        // 댓글 삭제
        commentService.delete(commentId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "댓글 삭제에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
