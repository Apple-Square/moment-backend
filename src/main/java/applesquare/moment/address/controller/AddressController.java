package applesquare.moment.address.controller;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.address.service.AddressService;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.exception.ResponseMap;
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
@RequestMapping("/api")
public class AddressController {
    private final AddressService addressService;


    /**
     * 카카오 주소 검색 API
     * @param keyword 검색어
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return  (status) 200,
     *                    (bdoy)    주소 검색 성공 메세지,
     *                                     검색된 주소 목록
     */
    @GetMapping("/addresses")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("keyword") String keyword,
                                                      @RequestParam(value = "page", required = false) Integer page,
                                                      @RequestParam(value = "size", required = false) Integer size) {
        // DTO 구성
        KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO=KakaoLocationSearchRequestDTO.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .build();

        // 입력 형식 검증
        Validator.validate(kakaoLocationSearchRequestDTO);

        // 주소 검색
        PageResponseDTO<AddressSearchResponseDTO> pageResponseDTO=addressService.search(kakaoLocationSearchRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "주소 검색에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());
        responseMap.put("totalCount", pageResponseDTO.getTotalCount());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
