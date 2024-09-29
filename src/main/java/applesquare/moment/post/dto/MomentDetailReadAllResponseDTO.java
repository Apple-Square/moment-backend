package applesquare.moment.post.dto;

import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MomentDetailReadAllResponseDTO {
    private Long id;  // 게시글 ID
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDateTime regDate;  // 등록 시간
    private String url;  // 첨부파일 URL (원본)
    private UserProfileReadResponseDTO writer;  // 작성자 프로필
    private boolean isFollowed;  // 팔로우 여부
    private String content;  // 내용
    private String address;  // 위치 정보
    private Double x;  // 경도
    private Double y;  // 위도
    private long likeCount;  // 좋아요 개수
    private boolean isLiked;  // 좋아요 여부
    private long commentCount;  // 댓글 개수
    private boolean isCommented;  // 댓글 작성 여부
}
