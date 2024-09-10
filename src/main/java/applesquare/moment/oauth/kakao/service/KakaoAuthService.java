package applesquare.moment.oauth.kakao.service;

import applesquare.moment.oauth.kakao.dto.KakaoUserInfoReadResponseDTO;

public interface KakaoAuthService {

    String getAccessToken(String authCode);
    KakaoUserInfoReadResponseDTO getUserInfoByToken(String accessToken);
}
