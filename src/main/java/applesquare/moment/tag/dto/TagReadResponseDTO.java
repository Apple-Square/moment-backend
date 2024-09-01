package applesquare.moment.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagReadResponseDTO {
    private Long id;
    private String name;
    private long usageCount;
}
