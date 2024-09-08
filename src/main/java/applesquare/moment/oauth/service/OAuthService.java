package applesquare.moment.oauth.service;

import applesquare.moment.user.dto.UserProfileReadResponseDTO;

public interface OAuthService {
    UserProfileReadResponseDTO loginWithKakao(String authCode);
}
