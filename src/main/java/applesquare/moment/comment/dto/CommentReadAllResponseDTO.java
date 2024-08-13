package applesquare.moment.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentReadAllResponseDTO {
    private Long id;
    private String content;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDateTime regDate;
}
