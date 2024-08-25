package applesquare.moment.user.dto;

import applesquare.moment.user.model.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoReadResponseDTO {
    private String id;
    private String nickname;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDateTime regDate;  // 가입일
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;
    private Gender gender;
    private String address;
    private String intro;
    private String profileImage;  // 프로필 이미지 URL
}
