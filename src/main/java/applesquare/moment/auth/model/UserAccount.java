package applesquare.moment.auth.model;

import applesquare.moment.auth.service.AuthService;
import applesquare.moment.common.BaseEntity;
import applesquare.moment.user.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class UserAccount extends BaseEntity {
    @Id
    @Column(length = AuthService.MAX_USERNAME_LENGTH, nullable = false, updatable=false)
    private String username;
    @Column(nullable = false, updatable = true)
    private String password;
    @Column(nullable = false, updatable = true)
    private String email;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_info_id", nullable = false, updatable = false)
    private UserInfo userInfo;
}