package applesquare.moment.sse.exception;

public class SseEmitterNotFoundException extends RuntimeException{
    public SseEmitterNotFoundException(String message){
        super(message);
    }
}
