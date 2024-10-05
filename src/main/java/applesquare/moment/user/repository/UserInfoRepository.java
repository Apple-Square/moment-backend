package applesquare.moment.user.repository;

import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.custom.CustomUserInfoRepository;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserInfoRepository extends JpaRepository<UserInfo, String>, CustomUserInfoRepository {
    boolean existsByNickname(String nickname);
    boolean existsById(String id);
}
