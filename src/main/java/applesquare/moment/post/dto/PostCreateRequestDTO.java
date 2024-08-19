package applesquare.moment.post.dto;

import applesquare.moment.post.service.PostService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequestDTO {
    @Size(min= PostService.MIN_CONTENT_LENGTH, max=PostService.MAX_CONTENT_LENGTH)
    private String content;
    @NotNull(message = "반드시 하나 이상의 파일을 등록해야 합니다.")
    @Size(min = 1, message = "반드시 하나 이상의 파일을 등록해야 합니다.")
    private List<MultipartFile> files;
}
