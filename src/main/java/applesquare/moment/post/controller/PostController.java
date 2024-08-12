package applesquare.moment.post.controller;

import applesquare.moment.exception.ResponseMap;
import applesquare.moment.post.dto.PostCreateRequestDTO;
import applesquare.moment.post.dto.PostUpdateRequestDTO;
import applesquare.moment.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;


    /**
     * 게시글 등록 API
     * @param postCreateRequestDTO 게시글 입력 정보
     * @return  (status) 201,
     *          (body)  게시글 등록 성공 메세지,
     *                  등록된 게시글 ID
     */
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody PostCreateRequestDTO postCreateRequestDTO){
        // 게시글 등록
        Long result= postService.create(postCreateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "게시글 등록에 성공했습니다.");
        responseMap.put("id", result);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 게시글 수정 API
     * @param postId 게시글 ID
     * @param postUpdateRequestDTO 게시글 변경 정보
     * @return  (status) 200,
     *          (body)  게시글 수정 성공 메세지,
     *                  수정된 게시글 ID
     */
    @PatchMapping(value = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequestDTO postUpdateRequestDTO){
        // 게시글 수정
        Long result=postService.update(postId, postUpdateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "게시글 수정에 성공했습니다.");
        responseMap.put("id", result);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 게시글 삭제 API
     * @param postId 게시글 ID
     * @return  (status) 200,
     *          (body) 게시글 삭제 성공 메세지
     */
    @DeleteMapping("{postId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long postId){
        // 게시글 삭제
        postService.delete(postId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "게시글 삭제에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
