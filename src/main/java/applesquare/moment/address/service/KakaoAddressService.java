package applesquare.moment.address.service;

import applesquare.moment.address.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.address.dto.KakaoLocationSearchResponseDTO;


public interface KakaoAddressService {
    KakaoLocationSearchResponseDTO searchLocationByKeyword(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO);
}
