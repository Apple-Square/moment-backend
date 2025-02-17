package applesquare.moment.chat.dto;

import applesquare.moment.file.model.MediaType;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedVideoPostReadResponseDTO extends SharedPostReadResponseDTO{
    private Long id;
    private UserProfileReadResponseDTO writer;
    private MediaType mediaType;
    private String thumbnailUrl;
}
