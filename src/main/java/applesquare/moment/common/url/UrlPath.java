package applesquare.moment.common.url;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UrlPath {
    FRONT_MAIN_PAGE(UrlSource.FRONT, "/"),
    FRONT_ERROR_500_PAGE(UrlSource.FRONT, "/error/500"),
    FRONT_RESET_PASSWORD_PAGE(UrlSource.FRONT, "/auth/reset-password"),
    BACK_FILE_BASE_URL(UrlSource.BACK, "/api/files"),
    KAKAO_LOGIN_REDIRECT_URI(UrlSource.BACK, "/api/oauth/kakao/callback"),
    NAVER_LOGIN_REDIRECT_URI(UrlSource.BACK, "/api/oauth/naver/callback"),
    KAKAO_ADDRESS_URL(UrlSource.EXTERNAL, "https://dapi.kakao.com/v2/local/search/address.json"),
    KAKAO_LOCATION_URL(UrlSource.EXTERNAL, "https://dapi.kakao.com/v2/local/search/keyword.json"),
    KAKAO_LOGIN_URL(UrlSource.EXTERNAL, "https://kauth.kakao.com/oauth/authorize"),
    KAKAO_TOKEN_URL(UrlSource.EXTERNAL, "https://kauth.kakao.com/oauth/token"),
    KAKAO_USER_INFO_URL(UrlSource.EXTERNAL, "https://kapi.kakao.com/v2/user/me"),
    NAVER_LOGIN_URL(UrlSource.EXTERNAL, "https://nid.naver.com/oauth2.0/authorize"),
    NAVER_TOKEN_URL(UrlSource.EXTERNAL, "https://nid.naver.com/oauth2.0/token"),
    NAVER_USER_INFO_URL(UrlSource.EXTERNAL, "https://openapi.naver.com/v1/nid/me");


    private final UrlSource source;
    private final String path;
}
