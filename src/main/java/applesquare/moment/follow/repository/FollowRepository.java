package applesquare.moment.follow.repository;

import applesquare.moment.follow.model.Follow;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long>, CustomFollowRepository {
    boolean existsByFollower_IdAndFollowee_Id(String followerId, String followeeId);
    void deleteByFollower_IdAndFollowee_Id(String followerId, String followeeId);

    long countByFollower_Id(String userId);
    long countByFollowee_Id(String userId);

    // 특정 유저의 팔로워 ID 목록 조회
    @Query("SELECT f.id AS followId, " +
                "f.follower.id AS followerId " +
            "FROM Follow f " +
            "WHERE f.followee.id=:userId " +
                "AND (:cursor IS NULL OR f.id<:cursor)")
    List<Tuple> findAllFollowerByUserId(@Param("userId") String userId,
                                        @Param("cursor") Long cursor,
                                        Pageable pageable);

    // 팔로워 목록 중에 내가 팔로우한 사용자의 ID 목록 조회
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