package applesquare.moment.address.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressSearchResponseDTO {
    private String addressName;  // 주소
    private String x;  // 경도
    private String y;  // 위도
}
