package applesquare.moment.follow.service.impl;

import applesquare.moment.common.service.SecurityService;
import applesquare.moment.exception.DuplicateDataException;
import applesquare.moment.follow.model.Follow;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.follow.service.FollowService;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final FollowRepository followRepository;
    private final UserInfoRepository userInfoRepository;
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
            new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+followerId+")");
        }
        if(!userInfoRepository.existsById(followeeId)){
            new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+followeeId+")");
        }

        // 이미 팔로우 취소한 상태인지 검사
        if(!followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)){
            throw new DuplicateDataException("이미 팔로우 취소한 사용자입니다. (id = "+followeeId+")");
        }

        // DB에서 Follow 엔티티 삭제
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);

        return followeeId;
    }
}
