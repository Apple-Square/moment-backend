package applesquare.moment.auth.dto;

import applesquare.moment.auth.service.AuthService;
import applesquare.moment.user.model.Gender;
import applesquare.moment.user.service.UserInfoService;
import applesquare.moment.util.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
public class UserCreateRequestDTO {
    @NotNull
    @Size(min= UserInfoService.MIN_NICKNAME_LENGTH, max=UserInfoService.MAX_NICKNAME_LENGTH)
    @Pattern(regexp = Validator.NICKNAME_PATTERN, message = "한글, 알파벳 대소문자, 숫자, 밑줄 (_), 하이픈 (-), 연속 길이 1인 중간 공백( )만 입력 가능합니다.")
    private String nickname;
    @NotNull
    @Size(min= AuthService.MIN_USERNAME_LENGTH, max = AuthService.MAX_USERNAME_LENGTH)
    @Pattern(regexp = Validator.USERNAME_PATTERN, message = "알파벳 대소문자, 숫자, 밑줄 (_), 하이픈 (-)만 입력 가능합니다.")
    private String username;
    @NotNull
    @Size(min = AuthService.MIN_PASSWORD_LENGTH, max = AuthService.MAX_PASSWORD_LENGTH)
    @Pattern(regexp = Validator.PASSWORD_PATTERN, message = "알파벳 대문자, 소문자, 숫자, 특수 문자 ( ! ? @ # $ % ^ & )를 모두 입력해야 합니다.")
    private String password;
    private LocalDate birth;
    private Gender gender;
    @Email
    private String email;
    private String address;
}
