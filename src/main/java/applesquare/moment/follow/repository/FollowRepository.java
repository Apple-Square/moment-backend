package applesquare.moment.follow.repository;

import applesquare.moment.follow.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(String followerId, String followeeId);
    void deleteByFollowerIdAndFolloweeId(String followerId, String followeeId);

    long countByFollowerId(String userId);
    long countByFolloweeId(String userId);
}
