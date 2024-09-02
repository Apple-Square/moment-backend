package applesquare.moment.address.service;

import applesquare.moment.address.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.address.dto.KakaoLocationSearchResponseDTO;

import java.io.UnsupportedEncodingException;

public interface KakaoAddressService {
    KakaoLocationSearchResponseDTO searchLocationByKeyword(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO) throws UnsupportedEncodingException;
}
