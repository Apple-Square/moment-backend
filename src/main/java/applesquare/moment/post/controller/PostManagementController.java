package applesquare.moment.post.controller;

import applesquare.moment.exception.ResponseMap;
import applesquare.moment.post.dto.PostCreateRequestDTO;
import applesquare.moment.post.dto.PostUpdateRequestDTO;
import applesquare.moment.post.service.PostManagementService;
import applesquare.moment.util.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostManagementController {
    private final PostManagementService postManagementService;


    /**
     * 게시글 등록 API
     * @param content 게시글 내용
     * @param files 첨부 파일
     * @return  (status) 201,
     *          (body)  게시글 등록 성공 메세지,
     *                  등록된 게시글 ID
     */
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> create(@RequestParam("files") List<MultipartFile> files,
                                                      @RequestParam(value = "content", required = false) String content,
                                                      @RequestParam(value = "tags", required = false) List<String> tags,
                                                      @RequestParam(value = "address", required = false) String address
    )throws Exception {
        // DTO 생성
        PostCreateRequestDTO postCreateRequestDTO=PostCreateRequestDTO.builder()
                .content(content)
                .files(files)
                .tags(tags)
                .address(address)
                .build();

        // DTO 유효성 검사
        Validator.validate(postCreateRequestDTO);

        // 게시글 등록
        Long result= postManagementService.create(postCreateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "게시글 등록에 성공했습니다.");
        responseMap.put("id", result);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMap.getMap());
    }

    /**
     * 게시글 수정 API
     * @param postId 게시글 ID
     * @param content 수정한 게시글 내용
     * @param urls 수정한 기존 첨부파일
     * @param files 새로 추가할 첨부파일
     * @return  (status) 200,
     *          (body)  게시글 수정 성공 메세지,
     *                  수정된 게시글 ID
     */
    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long postId,
                                                      @RequestParam(value = "content", required = false) String content,
                                                      @RequestParam(value = "urls", required = false) List<String> urls,
                                                      @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                                      @RequestParam(value = "tags", required = false) List<String> tags,
                                                      @RequestParam(value = "address", required = false) String address
    ) throws Exception {
        // DTO 생성
        PostUpdateRequestDTO postUpdateRequestDTO=PostUpdateRequestDTO.builder()
                .content(content)
                .urls(urls)
                .files(files)
                .tags(tags)
                .address(address)
                .build();

        // DTO 유효성 검사
        Validator.validate(postUpdateRequestDTO);

        // 게시글 수정
        Long result=postManagementService.update(postId, postUpdateRequestDTO);

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
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long postId) throws IOException{
        // 게시글 삭제
        postManagementService.delete(postId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "게시글 삭제에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    @PatchMapping("/{postId}/view")
    public ResponseEntity<Map<String, Object>> view(@PathVariable Long postId){
        // 게시글 조회수 1 증가시키기
        long viewCount=postManagementService.incrementViewCount(postId, 1);

        // 응답 객체 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "게시글 조회수를 증가시켰습니다.");
        responseMap.put("viewCount", viewCount);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
