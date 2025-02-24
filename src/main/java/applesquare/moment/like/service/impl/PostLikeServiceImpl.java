package applesquare.moment.like.service.impl;

import applesquare.moment.common.exception.DuplicateDataException;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.like.model.PostLike;
import applesquare.moment.like.model.PostLikeKey;
import applesquare.moment.like.repository.PostLikeRepository;
import applesquare.moment.like.service.PostLikeService;
import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.service.NotificationSendService;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.PostManagementService;
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
public class PostLikeServiceImpl implements PostLikeService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserInfoRepository userInfoRepository;
    private final PostManagementService postManagementService;
    private final SecurityService securityService;
    private final NotificationSendService notificationSendService;

    /**
     * 게시글 좋아요 누르기
     * [필요 권한 : 로그인 상태 & 작성자가 아닌 사람]
     *
     * @param postId 게시글 ID
     * @return 좋아요 누른 게시글 ID
     */
    @Override
    public Long like(Long postId){
        // 권한 검사 (로그인 상태 확인)
        String myUserId= securityService.getUserId();

        // 해당 게시글의 작성자가 아닌지 확인
        Post post=postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 게시글입니다. (id = "+postId+")"));
        if(postManagementService.isOwner(post, myUserId)){
            throw new AccessDeniedException("본인이 작성한 게시글에는 좋아요를 누를 수 없습니다.");
        }

        // 좋아요를 누른 적 있는지 확인
        PostLikeKey postLikeKey=PostLikeKey.builder()
                .postId(postId)
                .userId(myUserId)
                .build();
        if(postLikeRepository.existsById(postLikeKey)){
            throw new DuplicateDataException("이미 좋아요를 누른 게시글입니다. (id = "+postId+")");
        }

        // 좋아요 엔티티 생성
        PostLike postLike=PostLike.builder()
                .postId(postId)
                .userId(myUserId)
                .build();

        // 좋아요 엔티티 DB 저장
        PostLike result=postLikeRepository.save(postLike);

        // 좋아요 알림 전송
        UserInfo sender=userInfoRepository.findById(myUserId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+myUserId+")"));

        NotificationRequestDTO<Void> notificationRequestDTO=NotificationRequestDTO.<Void>builder()
                .type(NotificationType.POST_LIKE)
                .sender(sender)  // 송신자 == 좋아요 누른 사람
                .receiverId(post.getWriter().getId())  // 수신자 == 게시물 작성자
                .referenceId(postId.toString())  // 래퍼런스 ID == 게시물 ID
                .build();

        notificationSendService.notify(notificationRequestDTO);

        // 좋아요 누른 게시글 ID 반환
        return result.getPostId();
    }

    /**
     * 게시글 좋아요 취소하기
     * [필요 권한 : 로그인 상태]
     *
     * @param postId 게시글 ID
     */
    @Override
    public void unlike(Long postId){
        String userId= securityService.getUserId();

        // 좋아요를 누른 적 있는지 확인
        PostLikeKey postLikeKey=PostLikeKey.builder()
                .postId(postId)
                .userId(userId)
                .build();
        if(!postLikeRepository.existsById(postLikeKey)){
            throw new DuplicateDataException("좋아요를 누르지 않은 게시글입니다. (id = "+postId+")");
        }

        // DB 삭제
        postLikeRepository.deleteById(postLikeKey);
    }
}
