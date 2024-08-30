package applesquare.moment.tag.repository;

import applesquare.moment.tag.model.Tag;
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
}
