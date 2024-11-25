package applesquare.moment.auth.dto;

import applesquare.moment.auth.service.AuthService;
import applesquare.moment.util.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDTO {
    @NotNull
    @Size(min = AuthService.MIN_PASSWORD_LENGTH, max = AuthService.MAX_PASSWORD_LENGTH)
    @Pattern(regexp = Validator.PASSWORD_PATTERN, message = "알파벳 대문자, 소문자, 숫자, 특수 문자 ( ! ? @ # $ % ^ & )를 모두 입력해야 합니다.")
    private String newPassword;
    @NotNull
    @NotBlank
    private String token;
}
