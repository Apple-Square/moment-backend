package applesquare.moment.user.service.impl;

import applesquare.moment.common.service.SecurityService;
import applesquare.moment.exception.TokenException;
import applesquare.moment.file.service.FileService;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.user.dto.UserInfoReadResponseDTO;
import applesquare.moment.user.dto.UserInfoUpdateRequestDTO;
import applesquare.moment.user.dto.UserPageReadResponseDTO;
import applesquare.moment.user.model.Gender;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserInfoService;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final FileService fileService;
    private final SecurityService securityService;
    private final ModelMapper modelMapper;


    /**
     * 사용자 정보 편집
     * [필요 권한 : 로그인 상태 & 사용자 본인]
     *
     * @param userId 사용자 ID
     * @param userInfoUpdateRequestDTO 사용자 변경 정보
     * @return 수정된 사용자의 ID
     */
    @Override
    public String updateUserInfo(String userId, UserInfoUpdateRequestDTO userInfoUpdateRequestDTO){
        // 권한 검사
        String myUserId= securityService.getUserId();

        if(!myUserId.equals(userId)){
            throw new AccessDeniedException("사용자 본인의 정보만 수정할 수 있습니다.");
        }

        // 기존 UserInfo 엔티티 가져오기
        UserInfo oldUserInfo=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));

        // 새로운 UserInfo 엔티티 생성
        String newNickname=(userInfoUpdateRequestDTO.getNickname()!=null)? userInfoUpdateRequestDTO.getNickname():oldUserInfo.getNickname();
        LocalDate newBirth=(userInfoUpdateRequestDTO.getBirth()!=null)? userInfoUpdateRequestDTO.getBirth():oldUserInfo.getBirth();
        Gender newGender=(userInfoUpdateRequestDTO.getGender()!=null)? userInfoUpdateRequestDTO.getGender():oldUserInfo.getGender();
        String newAddress=(userInfoUpdateRequestDTO.getAddress()!=null)? userInfoUpdateRequestDTO.getAddress():oldUserInfo.getAddress();
        String newIntro=(userInfoUpdateRequestDTO.getIntro()!=null)?userInfoUpdateRequestDTO.getIntro():oldUserInfo.getIntro();

        UserInfo newUserInfo=oldUserInfo.toBuilder()
                .nickname(newNickname)
                .birth(newBirth)
                .gender(newGender)
                .address(newAddress)
                .intro(newIntro)
                .build();

        // DB 저장
        userInfoRepository.save(newUserInfo);

        // 리소스 ID 반환
        return userId;
    }

    /**
     * 유저 페이지 조회
     * @param userId 사용자 ID
     * @return 유저 페이지
     */
    @Override
    public UserPageReadResponseDTO readUserPageById(String userId){
        // 유저 정보 가져오기
        UserInfo userInfo=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));
        UserInfoReadResponseDTO userInfoReadResponseDTO=modelMapper.map(userInfo, UserInfoReadResponseDTO.class);

        // 프로필 사진 URL 설정하기
        String profileName=(userInfo.getProfileImage()!=null)?
                userInfo.getProfileImage().getFilename() : UserProfileService.DEFAULT_PROFILE_NAME;
        String profileImageURL=fileService.convertFilenameToUrl(profileName);

        userInfoReadResponseDTO=userInfoReadResponseDTO.toBuilder()
                .profileImage(profileImageURL)
                .build();

        // 게시글 수, 팔로워 수, 팔로잉 수 가져오기
        long postCount=postRepository.countByWriterId(userId);
        long followerCount=followRepository.countByFolloweeId(userId);
        long followingCount=followRepository.countByFollowerId(userId);

        // 팔로우 눌렀는지 여부 가져오기
        boolean followed=false;
        try{
            // 나의 유저 ID 가져오기
            String myUserId=securityService.getUserId();

            // 로그인한 경우
            followed=followRepository.existsByFollowerIdAndFolloweeId(myUserId, userId);

        }catch(TokenException e){
            // 로그인하지 않은 경우
            // 유저 페이지 조회는 로그인이 필요하지 않으므로 아무런 처리도 하지 않는다.
        }

        // 응답 DTO 생성
        UserPageReadResponseDTO userPageReadResponseDTO=UserPageReadResponseDTO.builder()
                .user(userInfoReadResponseDTO)
                .postCount(postCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .followed(followed)
                .build();

        return userPageReadResponseDTO;
    }

}
