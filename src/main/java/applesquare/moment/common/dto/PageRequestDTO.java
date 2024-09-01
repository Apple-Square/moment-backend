package applesquare.moment.common.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {
    private int size = 10;
    private Long cursor;
    private String keyword;
}
