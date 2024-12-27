package applesquare.moment.post.controller;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.post.dto.PostDetailReadAllResponseDTO;
import applesquare.moment.post.dto.PostThumbnailReadAllResponseDTO;
import applesquare.moment.post.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostSearchController {
    private final PostSearchService postSearchService;


    /**
     *  게시물 검색 API
     * @param type 게시물 조회 타입
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @param keyword 검색 키워드
     * @return  (status) 200,
     *              (body) 검색 성공 메세지,
     *                      게시물 목록
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchDetail(@RequestParam(value = "type", required = false, defaultValue = "DETAIL") PostReadType type,
                                                            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                            @RequestParam(value = "cursor", required = false) String cursor,
                                                            @RequestParam(value = "keyword", required = false) String keyword){
        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO=PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .keyword(keyword)
                .build();

        // 응답 객체 구성
        ResponseMap responseMap=new ResponseMap();

        switch (type){
            case DETAIL:
                // 게시글 세부사항 목록 검색
                PageResponseDTO<PostDetailReadAllResponseDTO> detailPageResponseDTO=postSearchService.searchDetail(pageRequestDTO);
                responseMap.put("content", detailPageResponseDTO.getContent());
                responseMap.put("hasNext", detailPageResponseDTO.isHasNext());
                break;
            case THUMBNAIL:
                // 게시글 썸네일 목록 검색
                PageResponseDTO<PostThumbnailReadAllResponseDTO> thumbPageResponseDTO=postSearchService.searchThumbnail(pageRequestDTO);
                responseMap.put("content", thumbPageResponseDTO.getContent());
                responseMap.put("hasNext", thumbPageResponseDTO.isHasNext());
                break;
        }

        responseMap.put("message", "게시글 검색에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
