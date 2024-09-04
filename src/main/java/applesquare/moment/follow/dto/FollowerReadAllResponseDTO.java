package applesquare.moment.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowerReadAllResponseDTO {
    private String id;
    private String nickname;
    private String profileImage;  // 프로필 이미지 URL
    private boolean followed;
}
