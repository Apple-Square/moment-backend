package applesquare.moment.auth.dto;

import applesquare.moment.user.service.UserInfoService;
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
public class NicknameValidateRequestDTO {
    @NotNull
    @Size(min= UserInfoService.MIN_NICKNAME_LENGTH, max=UserInfoService.MAX_NICKNAME_LENGTH)
    @Pattern(regexp = Validator.NICKNAME_PATTERN, message = "한글, 알파벳 대소문자, 숫자, 밑줄 (_), 하이픈 (-)만 입력 가능합니다.")
    private String nickname;
}
