package applesquare.moment.comment.controller;

import applesquare.moment.comment.dto.CommentCreateRequestDTO;
import applesquare.moment.comment.dto.CommentReadAllResponseDTO;
import applesquare.moment.comment.service.CommentService;
import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.exception.ResponseMap;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class PostCommentController {
    private final CommentService commentService;

    /**
     * 댓글 등록 API
     * @param postId 소속한 게시글 ID
     * @param commentCreateRequestDTO 댓글 입력 정보
     * @return  (status) 201,
     *          (body)  댓글 등록 성공 메세지,
     *                  등록된 댓글 ID
     */
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> create(@PathVariable Long postId, @Valid @RequestBody CommentCreateRequestDTO commentCreateRequestDTO){
        // 댓글 등록
        Long result= commentService.create(postId, commentCreateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "댓글 등록에 성공했습니다.");
        responseMap.put("id", result);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 댓글 목록 조회 API
     * @param postId 소속한 게시글 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return  (status) 200,
     *          (body)  댓글 목록 조회 성공 메세지,
     *                  댓글 목록 페이지
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> readAll(@PathVariable Long postId, PageRequestDTO pageRequestDTO){
        // 특정 게시글의 댓글 목록 조회
        PageResponseDTO<CommentReadAllResponseDTO> pageResponseDTO=commentService.readAll(postId, pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "댓글 목록 조회에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
