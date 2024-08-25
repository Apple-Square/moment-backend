package applesquare.moment.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageReadResponseDTO {
    private UserInfoReadResponseDTO user;
    private long postCount;
    private long followerCount;
    private long followingCount;
    private boolean followed;
}
