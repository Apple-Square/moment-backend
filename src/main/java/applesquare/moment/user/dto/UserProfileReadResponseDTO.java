package applesquare.moment.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileReadResponseDTO {
    private String id;
    private String nickname;
    private String profileImage;  // 프로필 이미지 URL
}
