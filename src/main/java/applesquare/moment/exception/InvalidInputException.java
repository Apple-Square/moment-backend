package applesquare.moment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidInputException extends RuntimeException {
    private final HttpStatus status;
    public InvalidInputException(HttpStatus status, String message){
        super(message);
        this.status=status;
    }
}
