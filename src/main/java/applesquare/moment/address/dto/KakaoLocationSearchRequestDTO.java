package applesquare.moment.address.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoLocationSearchRequestDTO {
    private String keyword;  // 검색을 원하는 키워드
    @Builder.Default
    private Integer page = 1;  // 페이지 번호
    @Builder.Default
    private Integer size = 10;  // 페이지 크기
}
