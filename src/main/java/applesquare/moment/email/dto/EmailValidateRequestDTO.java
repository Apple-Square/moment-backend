package applesquare.moment.email.dto;

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
public class EmailValidateRequestDTO {
    @NotNull
    @NotBlank
    private String email;
    @NotNull
    @NotBlank
    private String code;
}
