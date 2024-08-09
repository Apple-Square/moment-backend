package applesquare.moment.auth.security;

import applesquare.moment.auth.model.UserAccount;
import applesquare.moment.auth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount =userAccountRepository.findByUsername(username)
                .orElseThrow(()->new UsernameNotFoundException("존재하지 않는 username 입니다. (username = "+username+")"));
        return UserDetailsImpl.build(userAccount);
    }
}
