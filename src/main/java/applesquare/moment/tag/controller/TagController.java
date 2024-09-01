package applesquare.moment.tag.controller;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.exception.ResponseMap;
import applesquare.moment.tag.dto.TagReadResponseDTO;
import applesquare.moment.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;


    /**
     * 태그 검색 API
     * @param pageRequestDTO 페이지 요청 정보
     * @return  (status) 200,
     *          (body)  태그 검색 성공 메세지,
     *                  태그 검색 결과
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> search(PageRequestDTO pageRequestDTO){
        // 태그 검색
        PageResponseDTO<TagReadResponseDTO> pageResponseDTO=tagService.search(pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "태그 검색에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
