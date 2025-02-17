package applesquare.moment.follow.service.impl;

import applesquare.moment.auth.exception.TokenException;
import applesquare.moment.common.exception.DuplicateDataException;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.file.service.FileService;
import applesquare.moment.follow.dto.FollowReadAllResponseDTO;
import applesquare.moment.follow.model.Follow;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.follow.service.FollowService;
import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.service.NotificationSendService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserProfileService;
import com.querydsl.core.Tuple;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static applesquare.moment.follow.model.QFollow.follow;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UserInfoRepository userInfoRepository;
    private final FileService fileService;
    private final SecurityService securityService;
    private final UserProfileService userProfileService;
    private final NotificationSendService notificationSendService;


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
        if(followRepository.existsByFollower_IdAndFollowee_Id(followerId, followeeId)){
            throw new DuplicateDataException("이미 팔로우한 사용자입니다. (id = "+followeeId+")");
        }

        // 팔로우 엔티티 생성
        Follow follow=Follow.builder()
                .follower(follower)
                .followee(followee)
                .build();

        // 팔로우 엔티티 DB 저장
        followRepository.save(follow);

        // 팔로우 알림 전송
        NotificationRequestDTO notificationRequestDTO=NotificationRequestDTO.builder()
                .type(NotificationType.FOLLOW)
                .sender(follower)  // 송신자 == 팔로우 누른 사람(follower)
                .receiverId(followeeId)  // 수신자 == 팔로우 상대(followee)
                .referenceId(followerId)  // 래퍼런스 ID == 팔로워 ID
                .build();

        notificationSendService.notify(notificationRequestDTO);

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
        if(!followRepository.existsByFollower_IdAndFollowee_Id(followerId, followeeId)){
            throw new DuplicateDataException("이미 팔로우 취소한 사용자입니다. (id = "+followeeId+")");
        }

        // DB에서 Follow 엔티티 삭제
        followRepository.deleteByFollower_IdAndFollowee_Id(followerId, followeeId);

        return followeeId;
    }

    /**
     * 특정 사용자의 팔로워 목록 조회
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 팔로워 목록 페이지
     */
    @Override
    public PageResponseDTO<FollowReadAllResponseDTO> searchFollowerByKeyword(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize=pageRequestDTO.getSize()+1;
        String keyword=pageRequestDTO.getKeyword();
        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor=Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저의 팔로워 목록 조회
        List<Tuple> tuples=followRepository.searchFollowerByKeyword(userId, keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(tuples.size()>pageRequestDTO.getSize()){
            tuples.remove(tuples.size()-1);
            hasNext=true;
        }

        // 나의 유저 Id 가져오기
        String myUserId=null;
        try{
            myUserId=securityService.getUserId();
        }catch(TokenException e){
            // 로그인하지 않은 경우
            // 로그인이 필요하지 않으므로 아무런 처리도 하지 않는다.
        }

        // 조회한 팔로워 목록 중에서 내가 팔로우한 유저의 Id 목록 가져오기
        List<String> followerIds=tuples.stream().map((tuple)-> tuple.get(follow.follower.id)).toList();
        List<String> followedFollowerIds=(myUserId!=null)?
                followRepository.findAllFollowedFollowerIdByFollowerIdsAndUserId(followerIds, myUserId)
                : new LinkedList<>();

        // DTO 변환
        List<FollowReadAllResponseDTO> followReadAllResponseDTOS=tuples.stream().map((tuple)->{
            Long followId=tuple.get(follow.id);
            String followerId=tuple.get(follow.follower.id);
            String followerNickname=tuple.get(follow.follower.nickname);
            String followerProfileName=tuple.get(follow.follower.profileImage.filename);

            // 프로필 사진 URL 생성
            String profileName=(followerProfileName!=null)? followerProfileName : UserProfileService.DEFAULT_PROFILE_NAME;
            String profileImageURL=fileService.convertFilenameToUrl(profileName);

            return FollowReadAllResponseDTO.builder()
                    .followId(followId)
                    .userId(followerId)
                    .nickname(followerNickname)
                    .profileImage(profileImageURL)
                    .followed(followedFollowerIds.contains(followerId))
                    .build();
        }).toList();

        PageResponseDTO<FollowReadAllResponseDTO> pageResponseDTO=PageResponseDTO.<FollowReadAllResponseDTO>builder()
                .content(followReadAllResponseDTOS)
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
    public PageResponseDTO<FollowReadAllResponseDTO> searchFollowingByKeyword(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize=pageRequestDTO.getSize()+1;
        String keyword=pageRequestDTO.getKeyword();
        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor=Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저의 팔로잉 목록 조회
        List<Tuple> tuples=followRepository.searchFolloweeByKeyword(userId, keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(tuples.size()>pageRequestDTO.getSize()){
            tuples.remove(tuples.size()-1);
            hasNext=true;
        }

        // 나의 유저 Id 가져오기
        String myUserId=null;
        try{
            myUserId=securityService.getUserId();
        }catch(TokenException e){
            // 로그인하지 않은 경우
            // 로그인이 필요하지 않으므로 아무런 처리도 하지 않는다.
        }

        // 조회한 팔로잉 목록 중에서 내가 팔로우한 유저의 Id 목록 가져오기
        List<String> followeeIds=tuples.stream().map((tuple)-> tuple.get(follow.followee.id)).toList();
        List<String> followedFolloweeIds=(myUserId!=null)?
                followRepository.findAllFollowedFolloweeIdByFolloweeIdsAndUserId(followeeIds, myUserId)
                : new LinkedList<>();

        // DTO 변환
        List<FollowReadAllResponseDTO> followReadAllResponseDTOS =tuples.stream().map((tuple)->{
            Long followId=tuple.get(follow.id);
            String followeeId=tuple.get(follow.followee.id);
            String followeeNickname=tuple.get(follow.followee.nickname);
            String followeeProfileName=tuple.get(follow.followee.profileImage.filename);

            // 유저 프로필 DTO 변환
            String profileName=(followeeProfileName!=null)? followeeProfileName : UserProfileService.DEFAULT_PROFILE_NAME;
            String profileImageURL=fileService.convertFilenameToUrl(profileName);

            return FollowReadAllResponseDTO.builder()
                    .followId(followId)
                    .userId(followeeId)
                    .nickname(followeeNickname)
                    .profileImage(profileImageURL)
                    .followed(followedFolloweeIds.contains(followeeId))
                    .build();
        }).toList();

        PageResponseDTO<FollowReadAllResponseDTO> pageResponseDTO=PageResponseDTO.<FollowReadAllResponseDTO>builder()
                .content(followReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();

        return pageResponseDTO;
    }

    /**
     * 특정 사용자와 상호 팔로우 관계에 있는 사용자 프로필 목록 검색
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 상호 팔로우 상대의 프로필 목록 페이지
     */
    @Override
    public PageResponseDTO<UserProfileReadResponseDTO> searchMutualFollowerByKeyword(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize=pageRequestDTO.getSize()+1;

        String keyword=pageRequestDTO.getKeyword();

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor=Long.parseLong(pageRequestDTO.getCursor());
        }

        // 상호 팔로우 상대 목록 검색
        List<UserInfo> mutualFollowers=followRepository.searchMutualFollowers(userId, keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(mutualFollowers.size()>pageRequestDTO.getSize()){
            mutualFollowers.remove(mutualFollowers.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<UserProfileReadResponseDTO> mutualFollowerDTOs=mutualFollowers.stream()
                .map((mutualFollower)-> userProfileService.toUserProfileDTO(mutualFollower))
                .collect(Collectors.toList());

        // DTO 반환
        return PageResponseDTO.<UserProfileReadResponseDTO>builder()
                .content(mutualFollowerDTOs)
                .hasNext(hasNext)
                .build();
    }
}
