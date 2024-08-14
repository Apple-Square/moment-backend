package applesquare.moment.like.repository;

import applesquare.moment.like.model.CommentLike;
import applesquare.moment.like.model.CommentLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeKey> {
}
