package applesquare.moment.location.controller;

import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.common.exception.ResponseMap;
import applesquare.moment.location.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.location.dto.LocationSearchResponseDTO;
import applesquare.moment.location.service.LocationService;
import applesquare.moment.util.Validator;
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
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService locationService;


    /**
     * 카카오 장소 검색 API
     * @param keyword 검색어
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return  (status) 200,
     *                    (bdoy)    장소 검색 성공 메세지,
     *                                     장소 검색 목록
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("keyword") String keyword,
                                                      @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                      @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        // DTO 구성
        KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO=KakaoLocationSearchRequestDTO.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();

        // 입력 형식 검증
        Validator.validate(kakaoLocationSearchRequestDTO);

        // 장소 검색
        PageResponseDTO<LocationSearchResponseDTO> pageResponseDTO=locationService.searchLocation(kakaoLocationSearchRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "장소 검색에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());
        responseMap.put("totalCount", pageResponseDTO.getTotalCount());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}