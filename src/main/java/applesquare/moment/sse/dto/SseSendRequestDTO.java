package applesquare.moment.sse.dto;

import applesquare.moment.sse.service.SseCategory;
import applesquare.moment.sse.service.SseEvent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SseSendRequestDTO {
    @NotNull
    SseCategory sseCategory;
    @NotNull
    String receiverId;
    String lastEventId;
    @NotNull
    SseEvent sseEvent;
    @NotNull
    Object data;
}
