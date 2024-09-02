package applesquare.moment.location.service;

import applesquare.moment.location.dto.KakaoLocationSearchRequestDTO;
import applesquare.moment.location.dto.KakaoLocationSearchResponseDTO;

public interface KakaoLocationService {
    KakaoLocationSearchResponseDTO searchLocationByKeyword(KakaoLocationSearchRequestDTO kakaoLocationSearchRequestDTO);
}
