package applesquare.moment.post.dto;

import applesquare.moment.file.model.MediaType;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailReadAllResponseDTO {
    private Long id;  // 게시글 ID
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDateTime regDate;  // 등록 시간
    private String content;  // 내용
    private UserProfileReadResponseDTO writer;  // 작성자 프로필
    private List<String> tags;  // 태그 목록
    private MediaType mediaType;  // 미디어 타입
    private List<String> urls;  // 첨부파일 (원본)
    private String address;  // 위치 정보
    private Double x;  // 경도
    private Double y;  // 위도
    private long viewCount;  // 조회수
    private long commentCount;  // 댓글 개수
    private long likeCount;  // 좋아요 개수
    private boolean isCommented;  // 댓글 작성 여부
    private boolean isLiked;  // 좋아요 여부
}
