package applesquare.moment.address.service;

import applesquare.moment.address.dto.KakaoAddressSearchRequestDTO;
import applesquare.moment.address.dto.KakaoAddressSearchResponseDTO;


public interface KakaoAddressService {
    KakaoAddressSearchResponseDTO searchAddressByKeyword(KakaoAddressSearchRequestDTO kakaoAddressSearchRequestDTO);
}
