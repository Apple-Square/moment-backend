package applesquare.moment.user.service.impl;

import applesquare.moment.common.service.SecurityService;
import applesquare.moment.exception.TokenException;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.service.FileService;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.user.dto.UserInfoReadResponseDTO;
import applesquare.moment.user.dto.UserInfoUpdateRequestDTO;
import applesquare.moment.user.dto.UserPageReadResponseDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.Gender;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserInfoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
     * 나의 프로필 조회
     * [필요 권한 : 로그인 상태]
     *
     * @return 나의 프로필 정보
     */
    @Override
    public UserProfileReadResponseDTO readMyProfile(){
        String userId= securityService.getUserId();
        UserProfileReadResponseDTO userProfileReadResponseDTO=readProfileById(userId);
        return userProfileReadResponseDTO;
    }

    /**
     * 사용자 프로필 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 프로필
     */
    @Override
    public UserProfileReadResponseDTO readProfileById(String userId){
        // 엔티티 조회
        UserInfo userInfo=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));

        // 프로필 사진 URL 가져오기
        String profileName=(userInfo.getProfileImage()!=null)?
                userInfo.getProfileImage().getFilename() : UserInfoService.DEFAULT_PROFILE_NAME;
        String profileImageURL=fileService.convertFilenameToUrl(profileName);

        // DTO 변환
        UserProfileReadResponseDTO userProfileReadResponseDTO=modelMapper.map(userInfo, UserProfileReadResponseDTO.class);
        userProfileReadResponseDTO=userProfileReadResponseDTO.toBuilder()
                .profileImage(profileImageURL)
                .build();

        // DTO 반환
        return userProfileReadResponseDTO;
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
                userInfo.getProfileImage().getFilename() : UserInfoService.DEFAULT_PROFILE_NAME;
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
     * 사용자 프로필 사진 설정
     * [필요 권한 : 로그인 상태 & 사용자 본인]
     *
     * @param userId 사용자 ID
     * @param profileImage 프로필 사진
     * @return 사진을 설정한 사용자 ID
     */
    @Override
    public String updateProfileImage(String userId, MultipartFile profileImage){
        // 권한 검사
        String myUserId= securityService.getUserId();
        if(!myUserId.equals(userId)){
            throw new AccessDeniedException("사용자 본인의 프로필 사진만 설정할 수 있습니다.");
        }

        // 입력 형식 검사
        if(profileImage==null || !FileService.isImage(profileImage)){
            throw new IllegalArgumentException("프로필 사진으로는 JPEG, PNG, WEBP 타입만 입력 가능합니다.");
        }

        // 기존 UserInfo 엔티티 가져오기
        UserInfo oldUserInfo=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));
        StorageFile oldProfileImage=oldUserInfo.getProfileImage();

        String uploadFilename=null;
        try{
            // 저장소에 새로운 프로필 사진 업로드
            String url=fileService.upload(profileImage);

            // StorageFile 엔티티 생성
            uploadFilename=fileService.convertUrlToFilename(url);

            StorageFile newProfileImage=StorageFile.builder()
                    .filename(uploadFilename)
                    .originalFilename(profileImage.getOriginalFilename())
                    .contentType(profileImage.getContentType())
                    .fileSize(profileImage.getSize())
                    .uploader(oldUserInfo)
                    .ord(0)
                    .build();

            // 새로운 UserInfo 엔티티 생성
            UserInfo newUserInfo=oldUserInfo.toBuilder()
                    .profileImage(newProfileImage)
                    .build();

            // DB 저장
            userInfoRepository.save(newUserInfo);

        }catch(Exception exception){
            log.error(exception.getMessage());

            // 저장소에 새로 업로드한 파일이 있다면
            if(uploadFilename!=null){
                try{
                    // 업로드한 파일 삭제
                    fileService.delete(uploadFilename);
                } catch (IOException ioException){
                    // 만약 삭제하는 것도 실패했다면, 로그를 남긴다.
                    log.error(ioException.getMessage());
                }
            }

            throw exception;
        }

        // 기존에 프로필 이미지가 존재하는 상태였다면
        if(oldProfileImage!=null){
            // 기존 프로필 이미지를 저장소에서 삭제
            try{
                fileService.delete(oldProfileImage.getFilename());
            } catch (IOException ioException){
                // 만약 삭제에 실패했다면, 로그를 남긴다.
                log.error(ioException.getMessage());
            }
        }

        // 리소스 ID 반환
        return userId;
    }

    /**
     * 사용자 프로필 사진 설정 해제
     * [필요 권한 : 로그인 상태 & 사용자 본인]
     *
     * @param userId 사용자 ID
     * @throws IOException 저장소 파일 삭제에 실패했을 때 발생하는 예외
     */
    @Override
    public void deleteProfileImage(String userId) throws IOException{
        // 권한 검사
        String myUserId= securityService.getUserId();
        if(!myUserId.equals(userId)){
            throw new AccessDeniedException("사용자 본인의 프로필 사진만 설정 해제할 수 있습니다.");
        }

        // 기존 UserInfo 엔티티 가져오기
        UserInfo oldUserInfo=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));

        // 기존에 프로필 사진이 존재했다면
        StorageFile oldProfileImage=oldUserInfo.getProfileImage();
        if(oldProfileImage!=null){
            // 새로운 UserInfo 엔티티 생성
            UserInfo newUserInfo=oldUserInfo.toBuilder()
                    .profileImage(null)
                    .build();

            // DB 저장
            userInfoRepository.save(newUserInfo);

            // 저장소에서 프로필 사진 삭제
            try{
                fileService.delete(oldProfileImage.getFilename());
            } catch (IOException e){
                // 파일 삭제에 실패했으면 로그를 남긴다.
                log.error(e.getMessage());
                throw e;
            }

        }
    }
}
