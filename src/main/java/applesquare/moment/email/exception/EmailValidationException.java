package applesquare.moment.email.exception;

public class EmailValidationException extends  RuntimeException{
    public EmailValidationException(String message){
        super(message);
    }
}
