package applesquare.moment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TokenError {
    UNACCEPT(HttpStatus.UNAUTHORIZED, "토큰을 받지 못했습니다."),
    MALFORM(HttpStatus.UNAUTHORIZED, "잘못된 형식의 토큰입니다."),
    EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BADSIGN(HttpStatus.UNAUTHORIZED, "토큰 시그니처가 잘못됐습니다."),
    UNSUPPORTED(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다.");

    private final HttpStatus status;
    private final String message;
}
