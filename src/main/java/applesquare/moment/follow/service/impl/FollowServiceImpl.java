package applesquare.moment.follow.service.impl;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.common.service.SecurityService;
import applesquare.moment.exception.DuplicateDataException;
import applesquare.moment.exception.TokenException;
import applesquare.moment.file.service.FileService;
import applesquare.moment.follow.dto.FolloweeReadAllResponseDTO;
import applesquare.moment.follow.dto.FollowerReadAllResponseDTO;
import applesquare.moment.follow.model.Follow;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.follow.service.FollowService;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UserInfoRepository userInfoRepository;
    private final FileService fileService;
    private final SecurityService securityService;


    /**
     * 사용자 팔로우
     * [필요 권한 : 로그인 상태 & 본인이 아닌 사용자]
     *
     * @param followeeId 팔로우할 사용자
     * @return 팔로우한 사용자 ID
     */
    @Override
    public String follow(String followeeId){
        // 권한 검사
        String myUserId=securityService.getUserId();
        if(myUserId.equals(followeeId)){
            throw new AccessDeniedException("자기 자신은 팔로우할 수 없습니다.");
        }

        // 사용자 정보 가져오기
        String followerId=myUserId;
        UserInfo follower=userInfoRepository.findById(followerId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+followerId+")"));
        UserInfo followee=userInfoRepository.findById(followeeId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+followeeId+")"));

        // 이미 팔로우한 상태인지 검사
        if(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)){
            throw new DuplicateDataException("이미 팔로우한 사용자입니다. (id = "+followeeId+")");
        }

        // Follow 엔티티 생성
        Follow follow=Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

        // DB 저장
        followRepository.save(follow);

        // 팔로우한 사용자의 ID 반환
        return followeeId;
    }

    /**
     * 사용자 팔로우 취소
     * [필요 권한 : 로그인 상태 & 본인이 아닌 사용자]
     *
     * @param followeeId 팔로우 취소할 사용자
     * @return 팔로우 취소한 사용자 ID
     */
    @Override
    public String unfollow(String followeeId){
        // 권한 검사
        String myUserId=securityService.getUserId();
        if(myUserId.equals(followeeId)){
            throw new AccessDeniedException("자기 자신은 팔로우 취소할 수 없습니다.");
        }

        // 존재하는 사용자인지 검사
        String followerId=myUserId;
        if(!userInfoRepository.existsById(followerId)){
            throw new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+followerId+")");
        }
        if(!userInfoRepository.existsById(followeeId)){
            throw new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+followeeId+")");
        }

        // 이미 팔로우 취소한 상태인지 검사
        if(!followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)){
            throw new DuplicateDataException("이미 팔로우 취소한 사용자입니다. (id = "+followeeId+")");
        }

        // DB에서 Follow 엔티티 삭제
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);

        return followeeId;
    }

    /**
     * 특정 사용자의 팔로워 목록 조회
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 팔로워 목록 페이지
     */
    @Override
    public PageResponseDTO<FollowerReadAllResponseDTO> readFollowerPage(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize=pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=pageRequestDTO.getCursor();

        // 특정 유저의 팔로워 목록 조회
        List<Follow> follows=followRepository.findAllFollowerByFolloweeId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(follows.size()>pageRequestDTO.getSize()){
            follows.remove(follows.size()-1);
            hasNext=true;
        }

        // 조회한 팔로워 Id 목록 가져오기
        List<String> followerIds=follows.stream().map((follow)-> follow.getFollower().getId()).toList();

        // 나의 유저 Id 가져오기
        String myUserId=null;
        try{
            myUserId=securityService.getUserId();
        }catch(TokenException e){
            // 로그인하지 않은 경우
            // 로그인이 필요하지 않으므로 아무런 처리도 하지 않는다.
        }

        // 조회한 팔로워 목록 중에서 내가 팔로우한 유저의 Id 목록 가져오기
        List<String> followedFollowerIds=(myUserId!=null)?
                followRepository.findAllFollowedFollowerIdByFollowerIdsAndUserId(followerIds, myUserId)
                : new LinkedList<>();

        // DTO 변환
        List<FollowerReadAllResponseDTO> followerReadAllResponseDTOS=follows.stream().map((follow)->{
            UserInfo follower=follow.getFollower();

            // 프로필 사진 URL 생성
            String profileName=(follower.getProfileImage()!=null)?
                    follower.getProfileImage().getFilename() : UserProfileService.DEFAULT_PROFILE_NAME;
            String profileImageURL=fileService.convertFilenameToUrl(profileName);

            return FollowerReadAllResponseDTO.builder()
                    .id(follower.getId())
                    .nickname(follower.getNickname())
                    .profileImage(profileImageURL)
                    .followed(followedFollowerIds.contains(follower.getId()))
                    .build();
        }).toList();

        PageResponseDTO<FollowerReadAllResponseDTO> pageResponseDTO=PageResponseDTO.<FollowerReadAllResponseDTO>builder()
                .content(followerReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();

        return pageResponseDTO;
    }

    /**
     * 특정 사용자의 팔로잉 목록 조회
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 팔로잉 목록 페이지
     */
    @Override
    public PageResponseDTO<FolloweeReadAllResponseDTO> readFollowingPage(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize=pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=pageRequestDTO.getCursor();

        // 특정 유저의 팔로잉 목록 조회
        List<Follow> follows=followRepository.findAllFolloweeByFollowerId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(follows.size()>pageRequestDTO.getSize()){
            follows.remove(follows.size()-1);
            hasNext=true;
        }

        // 조회한 Followee Id 목록 가져오기
        List<String> followeeIds=follows.stream().map(follow -> follow.getFollowee().getId()).toList();

        // 나의 유저 Id 가져오기
        String myUserId=null;
        try{
            myUserId=securityService.getUserId();
        }catch(TokenException e){
            // 로그인하지 않은 경우
            // 로그인이 필요하지 않으므로 아무런 처리도 하지 않는다.
        }

        // 조회한 팔로잉 목록 중에서 내가 팔로우한 유저의 Id 목록 가져오기
        List<String> followedFolloweeIds=(myUserId!=null)?
                followRepository.findAllFollowedFolloweeIdByFolloweeIdsAndUserId(followeeIds, myUserId)
                : new LinkedList<>();

        // DTO 변환
        List<FolloweeReadAllResponseDTO> followeeReadAllResponseDTOS=follows.stream().map((follow)->{
            UserInfo followee=follow.getFollowee();

            // 유저 프로필 DTO 변환
            String profileName=(followee.getProfileImage()!=null)?
                    followee.getProfileImage().getFilename() : UserProfileService.DEFAULT_PROFILE_NAME;
            String profileImageURL=fileService.convertFilenameToUrl(profileName);

            return FolloweeReadAllResponseDTO.builder()
                    .id(followee.getId())
                    .nickname(followee.getNickname())
                    .profileImage(profileImageURL)
                    .followed(followedFolloweeIds.contains(followee.getId()))
                    .build();
        }).toList();

        PageResponseDTO<FolloweeReadAllResponseDTO> pageResponseDTO=PageResponseDTO.<FolloweeReadAllResponseDTO>builder()
                .content(followeeReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();

        return pageResponseDTO;
    }
}
