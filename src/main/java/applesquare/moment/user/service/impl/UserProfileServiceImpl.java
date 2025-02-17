package applesquare.moment.user.service.impl;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.service.FileService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserInfoRepository userInfoRepository;
    private final FileService fileService;
    private final SecurityService securityService;


    /**
     * UserInfo -> UserProfileReadResponseDTO
     * @param userInfo UserInfo
     * @return UserProfileReadResponseDTO
     */
    @Override
    public UserProfileReadResponseDTO toUserProfileDTO(UserInfo userInfo){
        // 프로필 사진 URL 가져오기
        String profileName=(userInfo.getProfileImage()!=null)?
                userInfo.getProfileImage().getFilename() : UserProfileService.DEFAULT_PROFILE_NAME;
        String profileImageURL=fileService.convertFilenameToUrl(profileName);

        // DTO 변환
        return UserProfileReadResponseDTO.builder()
                .id(userInfo.getId())
                .nickname(userInfo.getNickname())
                .profileImage(profileImageURL)
                .build();
    }

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

        return toUserProfileDTO(userInfo);
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
    public String updateProfileImage(String userId, MultipartFile profileImage) throws Exception {
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

        StorageFile newProfileImage=null;
        try{
            // 저장소에 새로운 프로필 사진 업로드
            newProfileImage=fileService.upload(profileImage, oldUserInfo);

            // 새로운 UserInfo 엔티티 생성
            UserInfo newUserInfo=oldUserInfo.toBuilder()
                    .profileImage(newProfileImage)
                    .build();

            // DB 저장
            userInfoRepository.save(newUserInfo);

        }catch(Exception e){
            log.error(e.getMessage());

            // 저장소에 새로 업로드한 파일이 있다면
            if(newProfileImage!=null){
                try{
                    // 업로드한 파일 삭제
                    fileService.delete(newProfileImage.getFilename());
                } catch (IOException ioException){
                    // 만약 삭제하는 것도 실패했다면, 로그를 남긴다.
                    log.error(ioException.getMessage());
                }
            }

            throw e;
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

    /**
     * 키워드로 사용자 프로필 검색
     * - 검색 속성 : 사용자 ID | 닉네임
     * - 정렬 기준 : 인기순 (팔로워가 많은 순)
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 사용자 프로필 목록
     */
    @Override
    public PageResponseDTO<UserProfileReadResponseDTO> search(PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        String cursor= pageRequestDTO.getCursor();
        String keyword= pageRequestDTO.getKeyword();

        // 키워드로 사용자 검색 (팔로워가 많은 순으로 정렬)
        List<UserInfo> userInfos=userInfoRepository.searchByKeyword(keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(userInfos.size()>pageRequestDTO.getSize()){
            userInfos.remove(userInfos.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<UserProfileReadResponseDTO> userProfileReadResponseDTOS=userInfos.stream()
                .map((userInfo)-> toUserProfileDTO(userInfo))
                .toList();

        return PageResponseDTO.<UserProfileReadResponseDTO>builder()
                .content(userProfileReadResponseDTOS)
                .hasNext(hasNext)
                .build();
    }
}
