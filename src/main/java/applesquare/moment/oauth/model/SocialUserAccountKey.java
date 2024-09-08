package applesquare.moment.oauth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserAccountKey implements Serializable {
    private String socialType;
    private String socialId;  // OAuth 제공 업체의 유저 ID
}
