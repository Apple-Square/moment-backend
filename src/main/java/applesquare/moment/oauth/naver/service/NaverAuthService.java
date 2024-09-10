package applesquare.moment.oauth.naver.service;

import applesquare.moment.oauth.naver.dto.NaverUserInfoReadResponseDTO;

public interface NaverAuthService {
    String getAccessToken(String code, String state);
    NaverUserInfoReadResponseDTO getUserInfoByToken(String accessToken);
}
