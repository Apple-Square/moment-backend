package applesquare.moment.address.service;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;

import java.io.UnsupportedEncodingException;

public interface AddressService {
    PageResponseDTO<AddressSearchResponseDTO> search(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO) throws UnsupportedEncodingException;
}
