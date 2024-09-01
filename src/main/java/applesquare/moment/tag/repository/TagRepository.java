package applesquare.moment.tag.repository;

import applesquare.moment.tag.model.Tag;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}