package applesquare.moment.address.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoAddressSearchResponseDTO {
    private List<Document> documents;
    private Meta meta;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {
        @JsonProperty("address_name")
        private String addressName;
        @JsonProperty("y")
        private String y;
        @JsonProperty("x")
        private String x;
        @JsonProperty("address_type")
        private String addressType;
        private Address address;
        private RoadAddress roadAddress;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @JsonProperty("address_name")
        private String addressName;
        @JsonProperty("region_1depth_name")
        private String region1depthName;
        @JsonProperty("region_2depth_name")
        private String region2depthName;
        @JsonProperty("region_3depth_name")
        private String region3depthName;
        @JsonProperty("mountain_yn")
        private String mountainYn;
        @JsonProperty("main_address_no")
        private String mainAddressNo;
        @JsonProperty("sub_address_no")
        private String subAddressNo;
        @JsonProperty("zip_code")
        private String zipCode;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoadAddress {
        @JsonProperty("address_name")
        private String addressName;
        @JsonProperty("region_1depth_name")
        private String region1depthName;
        @JsonProperty("region_2depth_name")
        private String region2depthName;
        @JsonProperty("region_3depth_name")
        private String region3depthName;
        @JsonProperty("road_name")
        private String roadName;
        @JsonProperty("underground_yn")
        private String undergroundYn;
        @JsonProperty("main_building_no")
        private String mainBuildingNo;
        @JsonProperty("sub_building_no")
        private String subBuildingNo;
        @JsonProperty("building_name")
        private String buildingName;
        @JsonProperty("zone_no")
        private String zoneNo;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        @JsonProperty("total_count")
        private int totalCount;
        @JsonProperty("pageable_count")
        private int pageableCount;
        @JsonProperty("is_end")
        private boolean isEnd;
    }
}
