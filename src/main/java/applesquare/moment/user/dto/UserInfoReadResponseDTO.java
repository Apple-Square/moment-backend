package applesquare.moment.user.dto;

import applesquare.moment.user.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoReadResponseDTO {
    private String id;
    private String nickname;
    private LocalDate birth;
    private Gender gender;
    private String address;
}
