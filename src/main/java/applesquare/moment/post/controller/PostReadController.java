package applesquare.moment.post.controller;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.post.dto.PostDetailReadAllResponseDTO;
import applesquare.moment.post.dto.PostThumbnailReadAllResponseDTO;
import applesquare.moment.post.service.PostReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostReadController {
    private final PostReadService postReadService;


    /**
     * 특정 게시물 조회 API
     * @param postId 게시물 ID
     * @return 게시물 상세 정보
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Object>> readOne(@PathVariable Long postId){
        PostDetailReadAllResponseDTO postDetailDTO=postReadService.read(postId);

        // 응답 객체 구성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("post", postDetailDTO);
        responseMap.put("message", "게시글 조회에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 게시물 목록 조회 API
     * @param type 게시물 조회 타입
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  조회 성공 메세지,
     *                  게시물 목록
     */
    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> readDetailAll(@RequestParam(value = "type", required = false, defaultValue = "DETAIL") PostReadType type,
                                                             @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                             @RequestParam(value = "cursor", required = false) String cursor){
        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO=PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .build();

        // 응답 객체 구성
        ResponseMap responseMap=new ResponseMap();

        // 게시글 조회 타입
        switch(type){
            case DETAIL:
                // 게시글 세부사항 목록 조회
                PageResponseDTO<PostDetailReadAllResponseDTO> detailPageResponseDTO=postReadService.readDetailAll(pageRequestDTO);
                responseMap.put("content", detailPageResponseDTO.getContent());
                responseMap.put("hasNext", detailPageResponseDTO.isHasNext());
                break;
            case THUMBNAIL:
                // 게시글 썸네일 목록 조회
                PageResponseDTO<PostThumbnailReadAllResponseDTO> thumbPageResponseDTO=postReadService.readThumbnailAll(pageRequestDTO);
                responseMap.put("content", thumbPageResponseDTO.getContent());
                responseMap.put("hasNext", thumbPageResponseDTO.isHasNext());
                break;
        }

        responseMap.put("message", "게시글 목록 조회에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 특정 유저가 작성한 게시물 목록 조회 API
     * @param userId 작성자 ID
     * @param type 게시물 조회 타입
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  조회 성공 메세지,
     *                  게시물 목록
     */
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<Map<String, Object>> readAllByWriter(@PathVariable("userId") String userId,
                                                               @RequestParam(value = "type", required = false, defaultValue = "DETAIL") PostReadType type,
                                                               @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                               @RequestParam(value = "cursor", required = false) String cursor){
        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO=PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .build();

        // 응답 객체 구성
        ResponseMap responseMap=new ResponseMap();

        // 게시글 조회 타입
        switch(type){
            case DETAIL:
                // 특정 유저가 작성한 게시글 목록 조회
                PageResponseDTO<PostDetailReadAllResponseDTO> detailPageResponseDTO=postReadService.readDetailAllByUser(userId, pageRequestDTO);
                responseMap.put("content", detailPageResponseDTO.getContent());
                responseMap.put("hasNext", detailPageResponseDTO.isHasNext());
                break;
            case THUMBNAIL:
                // 특정 유저가 작성한 게시글 썸네일 목록 조회
                PageResponseDTO<PostThumbnailReadAllResponseDTO> thumbnailPageResponseDTO=postReadService.readThumbnailAllByUser(userId, pageRequestDTO);
                responseMap.put("content", thumbnailPageResponseDTO.getContent());
                responseMap.put("hasNext", thumbnailPageResponseDTO.isHasNext());
                break;
        }

        responseMap.put("message", "게시글 목록 조회에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 특정 유저가 좋아요 누른 게시물 목록 조회 API
     * @param userId 사용자 ID
     * @param type 게시물 조회 타입
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  조회 성공 메세지,
     *                  게시물 목록
     */
    @GetMapping("/users/{userId}/liked-posts")
    public ResponseEntity<Map<String, Object>> readLikedPostAllByUser(@PathVariable("userId") String userId,
                                                                      @RequestParam(value = "type", required = false, defaultValue = "DETAIL") PostReadType type,
                                                                      @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                                      @RequestParam(value = "cursor", required = false) String cursor){
        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO=PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .build();

        // 응답 객체 구성
        ResponseMap responseMap=new ResponseMap();

        // 게시글 조회 타입
        switch(type){
            case DETAIL:
                // 특정 유저가 좋아요 누른 게시글 목록 조회
                PageResponseDTO<PostDetailReadAllResponseDTO> detailPageResponseDTO=postReadService.readLikedDetailAllByUser(userId, pageRequestDTO);
                responseMap.put("content", detailPageResponseDTO.getContent());
                responseMap.put("hasNext", detailPageResponseDTO.isHasNext());
                break;
            case THUMBNAIL:
                // 특정 유저가 좋아요 누른 게시글 썸네일 목록 조회
                PageResponseDTO<PostThumbnailReadAllResponseDTO> thumbnailPageResponseDTO=postReadService.readLikedThumbnailAllByUser(userId, pageRequestDTO);
                responseMap.put("content", thumbnailPageResponseDTO.getContent());
                responseMap.put("hasNext", thumbnailPageResponseDTO.isHasNext());
                break;
        }

        responseMap.put("message", "게시글 목록 조회에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
