package applesquare.moment.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomentThumbnailReadAllResponseDTO {
    private Long id;  // 게시글 ID
    private String url;  // 썸네일 URL
    private long viewCount;  // 조회수
}
