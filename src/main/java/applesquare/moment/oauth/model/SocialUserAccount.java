package applesquare.moment.oauth.model;

import applesquare.moment.common.model.BaseEntity;
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
@IdClass(SocialUserAccountKey.class)
@Table(name = "social_user_account")
public class SocialUserAccount extends BaseEntity {
    @Id
    private String socialType;
    @Id
    private String socialId;  // OAuth 제공 업체의 유저 ID
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_info_id", nullable = false, updatable = false)
    private UserInfo userInfo;
}
