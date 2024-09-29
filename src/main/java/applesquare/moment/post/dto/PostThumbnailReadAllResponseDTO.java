package applesquare.moment.post.dto;

import applesquare.moment.file.model.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostThumbnailReadAllResponseDTO {
    private Long id;  // 게시글 ID
    private String url;  // 썸네일 URL
    private MediaType mediaType;  // 미디어 타입
}
