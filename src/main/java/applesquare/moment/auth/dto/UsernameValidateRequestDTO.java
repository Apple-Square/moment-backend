package applesquare.moment.auth.dto;

import applesquare.moment.auth.service.AuthService;
import applesquare.moment.util.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsernameValidateRequestDTO {
    @NotNull
    @Size(min= AuthService.MIN_USERNAME_LENGTH, max = AuthService.MAX_USERNAME_LENGTH)
    @Pattern(regexp = Validator.USERNAME_PATTERN, message = "알파벳 대소문자, 숫자, 밑줄 (_), 하이픈 (-)만 입력 가능합니다.")
    private String username;
}
