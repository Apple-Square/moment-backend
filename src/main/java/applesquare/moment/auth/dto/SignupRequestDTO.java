package applesquare.moment.auth.dto;

import applesquare.moment.auth.service.AuthService;
import applesquare.moment.user.model.Gender;
import applesquare.moment.user.service.UserInfoService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDTO {
    @NotBlank
    @Size(min= UserInfoService.MIN_NICKNAME_LENGTH, max=UserInfoService.MAX_NICKNAME_LENGTH)
    @Pattern(regexp = "^[가-힣A-Za-z0-9_-]+$")
    private String nickname;
    @NotBlank
    @Size(min= AuthService.MIN_USERNAME_LENGTH, max = AuthService.MAX_USERNAME_LENGTH)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$")
    private String username;
    @NotBlank
    @Size(min = AuthService.MIN_PASSWORD_LENGTH, max = AuthService.MAX_PASSWORD_LENGTH)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!\\?@#\\$%\\^&])[A-Za-z\\d!\\?@#\\$%\\^&]+$")
    private String password;
    private LocalDate birth;
    private Gender gender;
    @NotBlank
    @Email
    private String email;
    private String address;
}
