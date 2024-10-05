package applesquare.moment.post.controller;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.exception.ResponseMap;
import applesquare.moment.post.dto.MomentDetailReadAllResponseDTO;
import applesquare.moment.post.dto.MomentThumbnailReadAllResponseDTO;
import applesquare.moment.post.service.MomentReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MomentController {
    private final MomentReadService momentReadService;


    /**
     * 모먼트 목록 조회 API
     * @param type 게시물 조회 타입
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  조회 성공 메세지,
     *                  모먼트 목록
     */
    @GetMapping("/posts/moments")
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
                // 모먼트 목록 조회
                PageResponseDTO<MomentDetailReadAllResponseDTO> pageResponseDTO=momentReadService.readDetailAll(pageRequestDTO);
                responseMap.put("content", pageResponseDTO.getContent());
                responseMap.put("hasNext", pageResponseDTO.isHasNext());
                break;
            case THUMBNAIL:
                throw new IllegalArgumentException("지원하지 않는 조회 타입입니다. (type="+type+")");
        }

        responseMap.put("message", "모먼트 목록 조회에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 특정 유저가 작성한 모먼트 목록 조회 API
     * @param userId 작성자 ID
     * @param type 게시물 조회 타입
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (body)  조회 성공 메세지,
     *                  모먼트 목록
     */
    @GetMapping("/users/{userId}/posts/moments")
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
                // 특정 유저가 작성한 모먼트 목록 조회
                PageResponseDTO<MomentDetailReadAllResponseDTO> detailPageResponseDTO=momentReadService.readDetailAllByUser(userId, pageRequestDTO);
                responseMap.put("content", detailPageResponseDTO.getContent());
                responseMap.put("hasNext", detailPageResponseDTO.isHasNext());
                break;
            case THUMBNAIL:
                // 특정 유저가 작성한 모먼트 썸네일 목록 조회
                PageResponseDTO<MomentThumbnailReadAllResponseDTO> thumbnailPageResponseDTO=momentReadService.readThumbnailAllByUser(userId, pageRequestDTO);
                responseMap.put("content", thumbnailPageResponseDTO.getContent());
                responseMap.put("hasNext", thumbnailPageResponseDTO.isHasNext());
                break;
        }

        responseMap.put("message", "모먼트 목록 조회에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 특정 유저가 좋아요 누른 모먼트 목록 조회 API
     * @param userId 사용자 ID
     * @param type 게시물 조회 타입
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @return  (status) 200,
     *          (bdoy)  조회 성공 메세지,
     *                  모먼트 목록
     */
    @GetMapping("/users/{userId}/liked-posts/moments")
    public ResponseEntity<Map<String, Object>> readLikedMomentAllByUser(@PathVariable("userId") String userId,
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
                // 특정 유저가 좋아요 누른 모먼트 목록 조회
                PageResponseDTO<MomentDetailReadAllResponseDTO> detailPageResponseDTO=momentReadService.readLikedDetailAllByUser(userId, pageRequestDTO);
                responseMap.put("content", detailPageResponseDTO.getContent());
                responseMap.put("hasNext", detailPageResponseDTO.isHasNext());
                break;
            case THUMBNAIL:
                throw new IllegalArgumentException("지원하지 않는 조회 타입입니다. (type="+type+")");
        }

        responseMap.put("message", "모먼트 목록 조회에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
