package applesquare.moment.post.dto;

import applesquare.moment.post.service.PostService;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDTO {
    @Size(min= PostService.MIN_CONTENT_LENGTH, max=PostService.MAX_CONTENT_LENGTH)
    private String content;
}
