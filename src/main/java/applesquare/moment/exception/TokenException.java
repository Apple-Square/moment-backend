package applesquare.moment.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

@Getter
public class TokenException extends RuntimeException {
    private final HttpStatus status;

    public TokenException(TokenError error){
        super(error.getMessage());
        this.status=error.getStatus();
    }

    public void sendErrorResponse(HttpServletResponse resp){
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setStatus(status.value());

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", getMessage());

        try{
            resp.getWriter().write(responseMap.toJson());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
