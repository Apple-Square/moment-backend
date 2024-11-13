package applesquare.moment.like.service.impl;

import applesquare.moment.comment.model.Comment;
import applesquare.moment.comment.repository.CommentRepository;
import applesquare.moment.comment.service.CommentService;
import applesquare.moment.common.exception.DuplicateDataException;
import applesquare.moment.common.service.SecurityService;
import applesquare.moment.like.model.CommentLike;
import applesquare.moment.like.model.CommentLikeKey;
import applesquare.moment.like.repository.CommentLikeRepository;
import applesquare.moment.like.service.CommentLikeService;
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
    private final SecurityService securityService;


    /**
     * 댓글 좋아요 누르기
     * [필요 권한 : 로그인 상태 & 작성자가 아닌 사람]
     *
     * @param commentId 댓글 ID
     * @return 좋아요 누른 댓글 ID
     */
    @Override
    public Long like(Long commentId){
        String userId= securityService.getUserId();

        // 해당 댓글의 작성자가 아닌지 확인
        Comment comment=commentRepository.findById(commentId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 댓글입니다. (id = "+commentId+")"));
        if(commentService.isOwner(comment, userId)){
            throw new AccessDeniedException("본인이 작성한 댓글에는 좋아요를 누를 수 없습니다.");
        }

        // 좋아요를 누른 적 있는지 확인
        CommentLikeKey commentLikeKey=CommentLikeKey.builder()
                .commentId(commentId)
                .userId(userId)
                .build();
        if(commentLikeRepository.existsById(commentLikeKey)){
            throw new DuplicateDataException("이미 좋아요를 누른 댓글입니다. (id = "+commentId+")");
        }

        // 엔티티 생성
        CommentLike commentLike=CommentLike.builder()
                .commentId(commentId)
                .userId(userId)
                .build();

        // DB 저장
        CommentLike result=commentLikeRepository.save(commentLike);

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
