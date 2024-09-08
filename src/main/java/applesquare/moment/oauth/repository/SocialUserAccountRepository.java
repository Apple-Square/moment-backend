package applesquare.moment.oauth.repository;

import applesquare.moment.oauth.model.SocialUserAccount;
import applesquare.moment.oauth.model.SocialUserAccountKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialUserAccountRepository extends JpaRepository<SocialUserAccount, SocialUserAccountKey> {
}
