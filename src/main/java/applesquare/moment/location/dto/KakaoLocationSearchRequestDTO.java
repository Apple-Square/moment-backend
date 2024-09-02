package applesquare.moment.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLocationSearchRequestDTO {
    @NotNull(message = "키워드를 입력해주세요.")
    @NotBlank(message = "키워드를 입력해주세요.")
    private String keyword;  // 검색을 원하는 키워드
    @Builder.Default
    private Integer page = 1;  // 페이지 번호
    @Builder.Default
    private Integer size = 10;  // 페이지 크기
}
