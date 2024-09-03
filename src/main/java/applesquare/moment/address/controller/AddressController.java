package applesquare.moment.address.controller;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.service.AddressService;
import applesquare.moment.exception.ResponseMap;
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


    @GetMapping("/address")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("keyword") String keyword){
        // 주소 검색
        AddressSearchResponseDTO addressSearchResponseDTO=addressService.searchAddress(keyword);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "장소 검색에 성공했습니다.");
        responseMap.put("address", addressSearchResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
