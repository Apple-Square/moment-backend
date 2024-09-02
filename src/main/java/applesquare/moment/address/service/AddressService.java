package applesquare.moment.address.service;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;

public interface AddressService {
    PageResponseDTO<AddressSearchResponseDTO> search(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO);
    AddressSearchResponseDTO readByAddress(String address);
}
