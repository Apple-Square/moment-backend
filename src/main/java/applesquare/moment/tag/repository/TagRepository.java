package applesquare.moment.tag.repository;

import applesquare.moment.tag.model.Tag;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    @Query("SELECT t " +
            "FROM Tag t " +
            "LEFT JOIN t.posts p " +
            "WHERE t.name IN :tagNames " +
                "AND p IS NULL")
    List<Tag> findUnreferencedTags(@Param("tagNames") List<String> tagNames);

    @Query("SELECT t AS tag, COUNT(p) AS postCount " +
            "FROM Tag t " +
            "LEFT JOIN t.posts p " +
            "WHERE t.name LIKE %:keyword% " +
                "AND (:cursor IS NULL OR t.id < :cursor) " +
            "GROUP BY t")
    List<Tuple> findByKeyword(@Param("keyword") String keyword,
                              @Param("cursor") Long cursor,
                              Pageable pageable);

    @Query(value = "SELECT t AS tag, COUNT(p) AS postCount " +
            "FROM Tag t " +
            "INNER JOIN t.posts p " +
            "WHERE (:baseTime IS NULL OR p.modDate >= :baseTime) " +
            "GROUP BY t " +
            "ORDER BY postCount DESC")
    List<Tuple> findPopularTags(@Param("baseTime") LocalDateTime baseTime,
                                Pageable pageable);

    @Query(value = "SELECT p.id AS postId, t.name AS tagName " +
            "FROM Post p " +
            "INNER JOIN post_tags pt ON p.id = pt.post_id " +
            "LEFT JOIN Tag t ON pt.tag_id = t.id " +
            "WHERE p.id IN :postIds " +
            "ORDER BY p.id, pt.tag_order", nativeQuery = true)
    List<Tuple> findTagAllByPostIds(@Param("postIds") List<Long> postIds);
}