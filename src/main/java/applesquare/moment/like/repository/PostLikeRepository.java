package applesquare.moment.like.repository;

import applesquare.moment.like.model.PostLike;
import applesquare.moment.like.model.PostLikeKey;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeKey> {
    @Query("SELECT pl.postId AS postId " +
            "FROM PostLike pl " +
            "WHERE pl.postId IN :postIds AND pl.userId=:userId")
    List<Long> findAllLikedPostIdByPostIdsAndUserId(@Param("postIds") List<Long> postIds,
                                                    @Param("userId") String userId);
    @Query("SELECT pl.postId AS postId, COUNT(pl) AS likeCount " +
            "FROM PostLike pl " +
            "WHERE pl.postId IN :postIds " +
            "GROUP BY pl.postId")
    List<Tuple> countByPostIds(@Param("postIds") List<Long> postIds);
}
