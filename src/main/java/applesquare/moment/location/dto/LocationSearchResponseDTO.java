package applesquare.moment.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearchResponseDTO {
    private String id;  // 장소 ID
    private String placeName;  // 장소명, 업체명
    private String addressName;  // 주소 이름
    private String roadAddressName;  // 도로명 주소
    private String categoryName;  // 카테고리 이름
    private String x;  // 경도
    private String y;  // 위도


}
