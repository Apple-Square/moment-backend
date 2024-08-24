package applesquare.moment.follow.repository;

import applesquare.moment.follow.model.Follow;
import applesquare.moment.user.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowee(UserInfo follower, UserInfo followee);
    void deleteByFollowerAndFollowee(UserInfo follower, UserInfo followee);
}
