package applesquare.moment.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailCodeRequestDTO {
    @NotNull
    @NotBlank
    @Email(message = "잘못된 형식의 이메일입니다.")
    private String email;
}
