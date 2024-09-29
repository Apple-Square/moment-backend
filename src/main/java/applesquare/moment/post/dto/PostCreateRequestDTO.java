package applesquare.moment.post.dto;

import applesquare.moment.post.service.PostManagementService;
import applesquare.moment.tag.service.TagService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Size(min = PostManagementService.MIN_CONTENT_LENGTH, max = PostManagementService.MAX_CONTENT_LENGTH)
    private String content;
    private String address;
    @NotNull(message = "반드시 하나 이상의 파일을 등록해야 합니다.")
    @Size(min = 1, message = "반드시 하나 이상의 파일을 등록해야 합니다.")
    private List<MultipartFile> files;
    @Size(max = PostManagementService.MAX_TAG_COUNT, message = "태그는 최대 10개까지 등록 가능합니다.")
    private List<
            @Size(min = TagService.MIN_TAG_NAME_LENGTH, max = TagService.MAX_TAG_NAME_LENGTH)
            @Pattern(regexp = "^[a-z0-9가-힣_]+$", message = "태그에는 한글, 영어 소문자, 숫자, 언더바(_)만 포함할 수 있습니다.")
                    String> tags;
}
