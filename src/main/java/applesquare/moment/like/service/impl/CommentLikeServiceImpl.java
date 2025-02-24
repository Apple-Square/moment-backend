package applesquare.moment.like.service.impl;

import applesquare.moment.comment.model.Comment;
import applesquare.moment.comment.repository.CommentRepository;
import applesquare.moment.comment.service.CommentService;
import applesquare.moment.common.exception.DuplicateDataException;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.like.model.CommentLike;
import applesquare.moment.like.model.CommentLikeKey;
import applesquare.moment.like.repository.CommentLikeRepository;
import applesquare.moment.like.service.CommentLikeService;
import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.service.NotificationSendService;
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
public class CommentLikeServiceImpl implements CommentLikeService {
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserInfoRepository userInfoRepository;
    private final SecurityService securityService;
    private final NotificationSendService notificationSendService;


    /**
     * 댓글 좋아요 누르기
     * [필요 권한 : 로그인 상태 & 작성자가 아닌 사람]
     *
     * @param commentId 댓글 ID
     * @return 좋아요 누른 댓글 ID
     */
    @Override
    public Long like(Long commentId){
        // 권한 검사 (로그인 상태 확인)
        String myUserId= securityService.getUserId();

        // 해당 댓글의 작성자가 아닌지 확인
        Comment comment=commentRepository.findById(commentId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 댓글입니다. (id = "+commentId+")"));
        if(commentService.isOwner(comment, myUserId)){
            throw new AccessDeniedException("본인이 작성한 댓글에는 좋아요를 누를 수 없습니다.");
        }

        // 좋아요를 누른 적 있는지 확인
        CommentLikeKey commentLikeKey=CommentLikeKey.builder()
                .commentId(commentId)
                .userId(myUserId)
                .build();
        if(commentLikeRepository.existsById(commentLikeKey)){
            throw new DuplicateDataException("이미 좋아요를 누른 댓글입니다. (id = "+commentId+")");
        }

        // 좋아요 엔티티 생성
        CommentLike commentLike=CommentLike.builder()
                .commentId(commentId)
                .userId(myUserId)
                .build();

        // 좋아요 엔티티 DB 저장
        CommentLike result=commentLikeRepository.save(commentLike);

        // 좋아요 알림 전송
        UserInfo sender=userInfoRepository.findById(myUserId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+myUserId+")"));

        NotificationRequestDTO<Void> notificationRequestDTO=NotificationRequestDTO.<Void>builder()
                .type(NotificationType.COMMENT_LIKE)
                .sender(sender)  // 송신자 == 좋아요 누른 사람
                .receiverId(comment.getWriter().getId())  // 수신자 == 댓글 작성자
                .referenceId(comment.getPost().getId().toString())  // 래퍼런스 ID == 게시물 ID
                .build();

        notificationSendService.notify(notificationRequestDTO);

        // 좋아요 누른 댓글 ID 반환
        return result.getCommentId();
    }

    /**
     * 댓글 좋아요 취소하기
     * [필요 권한 : 로그인 상태]
     *
     * @param commentId 댓글 ID
     */
    @Override
    public void unlike(Long commentId){
        String userId= securityService.getUserId();

        // 좋아요를 누른 적 있는지 확인
        CommentLikeKey commentLikeKey=CommentLikeKey.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        if(!commentLikeRepository.existsById(commentLikeKey)){
            throw new DuplicateDataException("좋아요를 누르지 않은 댓글입니다. (id = "+commentId+")");
        }

        // DB 삭제
        commentLikeRepository.deleteById(commentLikeKey);
    }
}
