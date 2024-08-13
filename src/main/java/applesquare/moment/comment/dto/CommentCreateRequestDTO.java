package applesquare.moment.comment.dto;

import applesquare.moment.comment.service.CommentService;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDTO {
    @Size(min = CommentService.MIN_CONTENT_LENGTH, max = CommentService.MAX_CONTENT_LENGTH)
    private String content;
}
