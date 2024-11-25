package applesquare.moment.email.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailDTO {
    @NotNull
    @NotBlank
    private String toEmail;
    @NotNull
    @NotBlank
    private String title;
    @NotNull
    @NotBlank
    private String message;
    @Builder.Default
    private boolean useHtml=false;
}
