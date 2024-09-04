package applesquare.moment.like.repository;

import applesquare.moment.like.model.CommentLike;
import applesquare.moment.like.model.CommentLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeKey> {
    @Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.commentId IN :commentIds AND cl.userId=:userId")
    List<Long> findAllLikedCommentIdByUserId(@Param("userId") String userId,
                                           @Param("commentIds") List<Long> commentIds);
}
