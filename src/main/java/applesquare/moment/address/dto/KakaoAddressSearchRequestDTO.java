package applesquare.moment.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoAddressSearchRequestDTO {
    @NotNull(message = "키워드를 입력해주세요.")
    @NotBlank(message = "키워드를 입력해주세요.")
    private String keyword;  // 검색을 원하는 질의어
    private String analyzeType;  // 검색 방식 (similar : 유사, exact : 정확)
    private Integer page;  // 페이지 번호 (기본값 : 1)
    private Integer size;  // 페이지 크기 (기본값 : 10)
}