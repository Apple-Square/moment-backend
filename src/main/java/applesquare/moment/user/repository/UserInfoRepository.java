package applesquare.moment.user.repository;

import applesquare.moment.user.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    boolean existsByNickname(String nickname);
}
