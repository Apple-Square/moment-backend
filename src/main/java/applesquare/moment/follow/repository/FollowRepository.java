package applesquare.moment.follow.repository;

import applesquare.moment.follow.model.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(String followerId, String followeeId);
    void deleteByFollowerIdAndFolloweeId(String followerId, String followeeId);

    long countByFollowerId(String userId);
    long countByFolloweeId(String userId);

    @Query("SELECT f " +
            "FROM Follow f " +
            "WHERE f.followee.id=:followeeId " +
                "AND (:cursor IS NULL OR f.id < :cursor)")
    List<Follow> findAllFollowerByFolloweeId(@Param("followeeId") String followeeId,
                                             @Param("cursor") Long cursor,
                                             Pageable pageable);

    @Query("SELECT f " +
            "FROM Follow f " +
            "WHERE f.follower.id=:followerId " +
                "AND (:cursor IS NULL OR f.id < :cursor)")
    List<Follow> findAllFolloweeByFollowerId(@Param("followerId") String followerId,
                                             @Param("cursor") Long cursor,
                                             Pageable pageable);

    @Query("SELECT f.followee.id " +
            "FROM Follow f " +
            "WHERE f.follower.id=:userId " +
                "AND f.followee.id IN :followerIds")
    List<String> findAllFollowedFollowerIdByFollowerIdsAndUserId(@Param("followerIds") List<String> followerIds,
                                                                 @Param("userId") String userId);

    @Query("SELECT f.followee.id " +
            "FROM Follow f " +
            "WHERE f.follower.id=:userId " +
                "AND f.followee.id IN :followeeIds")
    List<String> findAllFollowedFolloweeIdByFolloweeIdsAndUserId(@Param("followeeIds") List<String> followeeIds,
                                                                 @Param("userId") String userId);
}