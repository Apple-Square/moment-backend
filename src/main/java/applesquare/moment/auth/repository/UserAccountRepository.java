package applesquare.moment.auth.repository;

import applesquare.moment.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
}
