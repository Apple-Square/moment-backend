package applesquare.moment.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@NotNull
@AllArgsConstructor
public class FollowReadAllResponseDTO {
    private Long followId;  // 팔로우 ID (커서로 사용될 예정)
    private String userId;  // 유저 ID
    private String nickname;  // 유저 닉네임
    private String profileImage;  // 유저 프로필 이미지 URL
    private boolean followed;  // 팔로우했는지 여부
}
