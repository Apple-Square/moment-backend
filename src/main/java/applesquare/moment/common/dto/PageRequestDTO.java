package applesquare.moment.common.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {
    @Builder.Default
    private int size = 10;
    private Long cursor;
    private String keyword;
}
