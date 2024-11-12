package applesquare.moment.email.exception;

public class MailSendException extends RuntimeException{
    public MailSendException(String message){
        super(message);
    }
}
