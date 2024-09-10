package applesquare.moment.oauth.service.impl;

import applesquare.moment.oauth.kakao.dto.KakaoUserInfoReadResponseDTO;
import applesquare.moment.oauth.kakao.service.KakaoAuthService;
import applesquare.moment.oauth.model.SocialType;
import applesquare.moment.oauth.model.SocialUserAccount;
import applesquare.moment.oauth.model.SocialUserAccountKey;
import applesquare.moment.oauth.naver.dto.NaverUserInfoReadResponseDTO;
import applesquare.moment.oauth.naver.service.NaverAuthService;
import applesquare.moment.oauth.repository.SocialUserAccountRepository;
import applesquare.moment.oauth.service.OAuthService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserInfoService;
import applesquare.moment.user.service.UserProfileService;
import applesquare.moment.util.NicknameGenerator;
import applesquare.moment.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {
    private final SocialUserAccountRepository socialUserAccountRepository;
    private final UserInfoRepository userInfoRepository;
    private final KakaoAuthService kakaoAuthService;
    private final NaverAuthService naverAuthService;
    private final UserProfileService userProfileService;


    @Override
    public UserProfileReadResponseDTO loginWithKakao(String code){
        // 카카오 Access Token 발급
        String kakaoAccessToken= kakaoAuthService.getAccessToken(code);

        // 카카오 사용자 정보 조회하기
        KakaoUserInfoReadResponseDTO kakaoUserInfoDTO=kakaoAuthService.getUserInfoByToken(kakaoAccessToken);
        if(kakaoUserInfoDTO==null || kakaoUserInfoDTO.getId()==null){
            throw new OAuth2AuthenticationException("Kakao 사용자 정보 조회에 실패했습니다.");
        }

        // 이미 회원가입한 유저인지 조회
        String kakaoUserId=kakaoUserInfoDTO.getId().toString();
        SocialUserAccountKey socialUserAccountKey= SocialUserAccountKey.builder()
                .socialType(SocialType.KAKAO.name())
                .socialId(kakaoUserId)
                .build();
        Optional<SocialUserAccount> optionalSocialUserAccount=socialUserAccountRepository.findById(socialUserAccountKey);

        String userId;
        if(optionalSocialUserAccount.isPresent()){
            // 이미 존재하는 사용자이면
            userId=optionalSocialUserAccount.get().getUserInfo().getId();
        }
        else{
            // 존재하지 않는 사용자이면, 자동 회원가입
            userId=createSocialUser(SocialType.KAKAO.name(), kakaoUserId);
        }

        // 사용자 프로필 정보 반환
        return userProfileService.readProfileById(userId);
    }

    @Override
    public UserProfileReadResponseDTO loginWithNaver(String code, String state){
        // 네이버 Access Token 발급
        String naverAccessToken= naverAuthService.getAccessToken(code, state);

        // 네이버 사용자 정보 조회하기
        NaverUserInfoReadResponseDTO naverUserInfoDTO =naverAuthService.getUserInfoByToken(naverAccessToken);
        if(naverUserInfoDTO==null || naverUserInfoDTO.getResponse().getId()==null){
            throw new OAuth2AuthenticationException("Naver 사용자 정보 조회에 실패했습니다.");
        }

        // 이미 회원가입한 유저인지 조회
        String naverUserId=naverUserInfoDTO.getResponse().getId();
        SocialUserAccountKey socialUserAccountKey= SocialUserAccountKey.builder()
                .socialType(SocialType.NAVER.name())
                .socialId(naverUserId)
                .build();
        Optional<SocialUserAccount> optionalSocialUserAccount=socialUserAccountRepository.findById(socialUserAccountKey);

        String userId;
        if(optionalSocialUserAccount.isPresent()){
            // 이미 존재하는 사용자이면
            userId=optionalSocialUserAccount.get().getUserInfo().getId();
        }
        else{
            // 존재하지 않는 사용자이면, 자동 회원가입
            userId=createSocialUser(SocialType.NAVER.name(), naverUserId);
        }

        // 사용자 프로필 정보 반환
        return userProfileService.readProfileById(userId);
    }

    private String createSocialUser(String socialType, String socialUserId){
        // 중복되지 않는 사용자 ID 생성
        String userId;
        do {
            userId= StringUtil.generateRandomString(StringUtil.USER_ID_CHARACTERS, UserInfoService.USER_ID_LENGTH);
        }while(userInfoRepository.existsById(userId));

        // 중복되지 않는 닉네임 생성
        String nickname;
        do {
            nickname= NicknameGenerator.generateNickname();
        }while(userInfoRepository.existsByNickname(nickname));

        // UserInfo 엔티티 생성 (닉네임, 프로필 사진은 자동 설정)
        UserInfo userInfo=UserInfo.builder()
                .id(userId)
                .nickname(nickname)
                .social(true)
                .build();

        // SocialUserAccount 엔티티 생성
        SocialUserAccount socialUserAccount= SocialUserAccount.builder()
                .socialType(socialType)
                .socialId(socialUserId)
                .userInfo(userInfo)
                .build();

        // DB 저장
        socialUserAccountRepository.save(socialUserAccount);

        // 등록된 사용자 ID 반환
        return userId;
    }
}
