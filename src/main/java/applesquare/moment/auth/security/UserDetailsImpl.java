package applesquare.moment.auth.security;

import applesquare.moment.auth.model.UserAccount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private String id;  // 사용자 식별 아이디
    private String username;  // 계정 아이디
    @JsonIgnore
    private String password;
    private String email;

    public static UserDetailsImpl build(UserAccount userAccount){
        return new UserDetailsImpl(
                userAccount.getUserInfo().getId(),
                userAccount.getUsername(),
                userAccount.getPassword(),
                userAccount.getEmail()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재 프로젝트에서는 Authority를 설정할 일이 없어서 빈 Collection을 반환
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
