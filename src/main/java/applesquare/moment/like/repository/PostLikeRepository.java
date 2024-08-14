package applesquare.moment.like.repository;

import applesquare.moment.like.model.PostLike;
import applesquare.moment.like.model.PostLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeKey> {
}
