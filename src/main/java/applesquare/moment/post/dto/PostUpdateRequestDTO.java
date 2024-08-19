package applesquare.moment.post.dto;

import applesquare.moment.post.service.PostService;
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
public class PostUpdateRequestDTO {
    @Size(min= PostService.MIN_CONTENT_LENGTH, max=PostService.MAX_CONTENT_LENGTH)
    private String content;
    private List<String> urls;
    private List<MultipartFile> files;
}
