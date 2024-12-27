package applesquare.moment.post.repository;

import applesquare.moment.post.model.Post;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {
    long countByWriterId(String userId);

    // 게시물 목록 조회 (커서 페이징)
    @EntityGraph(attributePaths = {"files"})
    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE (:cursor IS NULL OR p.id<:cursor)")
    List<Post> findAll(@Param("cursor") Long cursor, Pageable pageable);

    // 게시물 썸네일 목록 조회 (커서 페이징)
    @Query(value = "SELECT p.id AS postId, " +
                "sf.filename AS filename, " +
                "sf.content_type AS contentType " +
            "FROM post p " +
            "INNER JOIN post_files pf ON pf.post_id=p.id " +
            "INNER JOIN storage_file sf ON pf.file_id=sf.id " +
            "WHERE pf.file_order=0 " +
            "AND (:cursor IS NULL OR p.id<:cursor)", nativeQuery = true)
    List<Tuple> findAllWithFirstFile(@Param("cursor") Long cursor, Pageable pageable);

    // 비디오 게시물 목록 조회 (커서 페이징)
    @EntityGraph(attributePaths = {"files"})
    @Query("SELECT p " +
            "FROM Post p " +
            "INNER JOIN p.files sf " +
            "WHERE sf.contentType LIKE 'video%' " +
            "AND (:cursor IS NULL OR p.id<:cursor)")
    List<Post> findMomentAll(@Param("cursor") Long cursor,
                             Pageable pageable);

    // ====================================================================

    // Post ID 목록으로 게시글 목록 조회
    @Query(value = "SELECT * " +
            "FROM post p " +
            "WHERE p.id IN :postIds " +
            "ORDER BY p.id DESC", nativeQuery = true)
    List<Post> findAllByPostIds(@Param("postIds") List<Long> postIds);

    // Post ID 목록으로 게시물 썸네일 목록 조회
    @Query(value = "SELECT p.id AS postId, " +
                "sf.filename AS filename, " +
                "sf.content_type AS contentType " +
            "FROM post p " +
                "INNER JOIN post_files pf ON pf.post_id=p.id " +
                "INNER JOIN storage_file sf ON pf.file_id=sf.id " +
            "WHERE p.id IN :postIds " +
                "AND pf.file_order=0 " +
            "ORDER BY p.id DESC", nativeQuery = true)
    List<Tuple> findAllByPostIdsWithFirstFile(@Param("postIds") List<Long> postIds);

    // ====================================================================

    // 작성자 ID로 게시물 목록 조회 (커서 페이징)
    @EntityGraph(attributePaths = {"files"})
    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE (:cursor IS NULL OR p.id<:cursor) " +
                "AND p.writer.id=:writerId")
    List<Post> findAllByWriterId(@Param("writerId") String writerId,
                                 @Param("cursor") Long cursor,
                                 Pageable pageable);

    // 작성자 ID로 게시물 썸네일 목록 조회 (커서 페이징)
    @Query(value = "SELECT p.id AS postId, " +
                "sf.filename AS filename, " +
                "sf.content_type AS contentType " +
            "FROM post p " +
            "INNER JOIN post_files pf ON pf.post_id=p.id " +
            "INNER JOIN storage_file sf ON pf.file_id=sf.id " +
            "WHERE p.writer_id=:writerId " +
                "AND (:cursor IS NULL OR p.id<:cursor) " +
                "AND pf.file_order=0", nativeQuery = true)
    List<Tuple> findAllWithFirstFileByWriterId(@Param("writerId") String writerId,
                                               @Param("cursor") Long cursor,
                                               Pageable pageable);

    // 작성자 ID에 따라 비디오 게시물 목록 조회 (커서 페이징)
    @EntityGraph(attributePaths = {"files"})
    @Query("SELECT p " +
            "FROM Post p " +
            "INNER JOIN p.files sf " +
            "WHERE p.writer.id=:writerId " +
            "AND sf.contentType LIKE 'video%' " +
            "AND (:cursor IS NULL OR p.id<:cursor)")
    List<Post> findMomentAllByWriterId(@Param("writerId") String writerId,
                                       @Param("cursor") Long cursor,
                                       Pageable pageable);

    // 작성자 ID에 따라 비디오 게시물 썸네일 목록 조회
    @Query(value = "SELECT p.id AS postId, " +
            "p.view_count AS viewCount, " +
            "sf.filename AS filename " +
            "FROM post p " +
            "INNER JOIN post_files pf ON pf.post_id=p.id " +
            "INNER JOIN storage_file sf ON pf.file_id=sf.id " +
            "WHERE p.writer_id=:writerId " +
            "AND (:cursor IS NULL OR p.id<:cursor) " +
            "AND pf.file_order=0 " +
            "AND sf.content_type LIKE 'video%'", nativeQuery = true)
    List<Tuple> findMomentAllWithFirstFileByWriterId(@Param("writerId") String writerId,
                                                     @Param("cursor") Long cursor,
                                                     Pageable pageable);

    // ====================================================================

    // 특정 유저가 좋아요 누른 게시물 목록 조회 (커서 페이징)
    @EntityGraph(attributePaths = {"files"})
    @Query("SELECT p " +
            "FROM Post p " +
            "INNER JOIN PostLike pl " +
                "ON p.id=pl.postId " +
            "WHERE pl.userId=:userId " +
                "AND (:cursor IS NULL OR p.id<:cursor)")
    List<Post> findLikedPostAllByUserId(@Param("userId") String userId,
                                        @Param("cursor") Long cursor,
                                        Pageable pageable);

    // 특정 유저가 좋아요 누른 게시물 썸네일 목록 조회 (커서 페이징)
    @Query(value = "SELECT p.id AS postId, " +
                "sf.filename AS filename, " +
                "sf.content_type AS contentType " +
            "FROM post p " +
            "INNER JOIN post_files pf ON pf.post_id=p.id " +
            "INNER JOIN storage_file sf ON pf.file_id=sf.id " +
            "INNER JOIN post_like pl " +
                "ON p.id=pl.post_id " +
            "WHERE pl.user_id=:userId " +
                "AND (:cursor IS NULL OR p.id<:cursor) " +
                "AND pf.file_order=0", nativeQuery = true)
    List<Tuple> findLikedPostAllWithFirstFileByUserId(@Param("userId") String userId,
                                                     @Param("cursor") Long cursor,
                                                     Pageable pageable);

    // 특정 유저가 좋아요 누른 비디오 게시물 목록 조회 (커서 페이징)
    @EntityGraph(attributePaths = {"files"})
    @Query("SELECT p " +
            "FROM Post p " +
            "INNER JOIN PostLike pl ON p.id=pl.postId " +
            "INNER JOIN p.files sf " +
            "WHERE pl.userId=:userId " +
            "AND (:cursor IS NULL OR p.id<:cursor) " +
            "AND sf.contentType LIKE 'video%'")
    List<Post> findLikedMomentAllByUserId(@Param("userId") String userId,
                                          @Param("cursor") Long cursor,
                                          Pageable pageable);
}
