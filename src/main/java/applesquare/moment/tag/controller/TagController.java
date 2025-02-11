package applesquare.moment.tag.controller;

import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.tag.dto.TagReadResponseDTO;
import applesquare.moment.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;


    /**
     * 태그 검색 API
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @param keyword 검색 키워드
     * @return  (status) 200,
     *          (body)  태그 검색 성공 메세지,
     *                  태그 검색 결과
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                      @RequestParam(value = "cursor", required = false) String cursor,
                                                      @RequestParam(value = "keyword", required = false) String keyword){
        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO=PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .keyword(keyword)
                .build();

        // 태그 검색
        PageResponseDTO<TagReadResponseDTO> pageResponseDTO=tagService.search(pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "태그 검색에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 인기 태그 목록 조회 API
     * @param days 며칠 전 기록부터 조회할 것인지
     * @param size 조회할 태그 개수
     * @return  (status) 200,
     *          (body)  인기 태그 목록 조회 성공 메세지,
     *                  인기 태그 목록
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> readPopularTags(@RequestParam(value = "days", required = false) Integer days,
                                                               @RequestParam(value = "size", required = false) Integer size){
        // 인기 태그 목록 조회
        List<TagReadResponseDTO> tagReadResponseDTOS=tagService.readPopularTags(days, size);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "인기 태그 목록 조회에 성공했습니다.");
        responseMap.put("tags", tagReadResponseDTOS);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
