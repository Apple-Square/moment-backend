package applesquare.moment.location.service;

import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.location.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.location.dto.LocationSearchResponseDTO;

public interface LocationService {
    PageResponseDTO<LocationSearchResponseDTO> searchLocation(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO);
}
