package applesquare.moment.comment.repository;

import applesquare.moment.comment.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c " +
            "WHERE c.post.id=:postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Query("SELECT c " +
            "FROM Comment c " +
            "WHERE c.post.id=:postId " +
                "AND (:cursor IS NULL OR c.id < :cursor)")
    List<Comment> findAllByPostId(@Param("postId") Long postId,
                                  @Param("cursor") Long cursor,
                                  Pageable pageable);
}
