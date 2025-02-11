package applesquare.moment.user.service.impl;

import applesquare.moment.auth.exception.TokenException;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.file.service.FileService;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.user.dto.UserInfoReadResponseDTO;
import applesquare.moment.user.dto.UserPageReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserPageService;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserPageServiceImpl implements UserPageService {
    private final UserInfoRepository userInfoRepository;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final FileService fileService;
    private final SecurityService securityService;
    private final ModelMapper modelMapper;


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
        long followerCount=followRepository.countByFollowee_Id(userId);
        long followingCount=followRepository.countByFollower_Id(userId);

        // 팔로우 눌렀는지 여부 가져오기
        boolean followed=false;
        try{
            // 나의 유저 ID 가져오기
            String myUserId=securityService.getUserId();

            // 로그인한 경우
            followed=followRepository.existsByFollower_IdAndFollowee_Id(myUserId, userId);

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
