package applesquare.moment.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@NotNull
@AllArgsConstructor
public class FolloweeReadAllResponseDTO {
    private String id;
    private String nickname;
    private String profileImage;  // 프로필 이미지 URL
    private boolean followed;
}
