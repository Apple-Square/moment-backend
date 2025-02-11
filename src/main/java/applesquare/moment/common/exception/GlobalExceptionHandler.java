package applesquare.moment.common.exception;

import applesquare.moment.auth.exception.TokenException;
import applesquare.moment.email.exception.EmailValidationException;
import applesquare.moment.email.exception.MailSendException;
import applesquare.moment.file.exception.FileTransferException;
import applesquare.moment.sse.exception.SseEmitterNotFoundException;
import applesquare.moment.sse.exception.SseSendException;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Validation 도중 발생한 예외 처리
     * @param e MethodArgumentNotValidException
     * @return 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        // 에러 로그 출력
        e.printStackTrace();

        final List<String> errorMessages=new LinkedList<>();

        BindingResult bindingResult=e.getBindingResult();
        bindingResult.getFieldErrors().forEach((fieldError)->{
            errorMessages.add(fieldError.getField()+": "+fieldError.getDefaultMessage()+".");
        });

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", errorMessages);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.getMap());
    }

    /**
     * Validation 도중 발생한 예외 처리
     * @param e ConstraintViolationException
     * @return 400 (Bad Request)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String,Object>> handleConstraintViolationException(ConstraintViolationException e){
        // 에러 로그 출력
        e.printStackTrace();

        final List<String> errorMessages=new LinkedList<>();

        Set<ConstraintViolation<?>> violations=e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errorMessages.add(fieldName+": "+errorMessage);
        }

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", errorMessages);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.getMap());
    }

    /**
     * 잘못된 요청 예외 처리
     * @param e IllegalArgumentException
     * @return 400 (Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.getMap());
    }

    /**
     * 컨트롤러에 필요한 Param이 들어오지 않았을 때 발생하는 예외 처리
     * @param e MissingServletRequestPartException
     * @return 400 (Bad Request)
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestPartException(MissingServletRequestPartException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.getMap());
    }

    /**
     * 업로드 용량 초과 예외 처리
     * @param e MaxUploadSizeExceededException
     * @return 400 (Bad Request)
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "파일이 업로드 가능한 용량을 초과했습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.getMap());
    }

    /**
     * 요청에 필요한 파라미터를 입력하지 않은 경우 발생하는 예외 처리
     * @param e MissingServletRequestParameterException
     * @return 400 (Bad Request)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.getMap());
    }

    /**
     * 이메일 관련 예외 처리
     * @param e 이메일 관련 Exception
     * @return 400 (Bad Request)
     */
    @ExceptionHandler({
            EmailValidationException.class,
            MailSendException.class,
            MessagingException.class
    })
    public ResponseEntity<Map<String, Object>> handleMailException(Exception e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap.getMap());
    }

    /**
     * JWT 인증에서 발생한 Token 예외 처리
     * @param e TokenException
     * @return 401 (Unauthorized)
     */
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<Map<String, Object>> handleTokenException(TokenException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(responseMap.getMap());
    }

    /**
     * 소셜 로그인에서 발생한 예외 처리
     * @param e OAuth2AuthenticationException
     * @return 401 (Unauthorized)
     */
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleOAuth2AuthenticationException(OAuth2AuthenticationException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap.getMap());
    }

    /**
     * 접근 거부 예외 처리
     * @param e AccessDeniedException
     * @return 403 (Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMap.getMap());
    }

    /**
     * 존재하지 않는 API를 요청한 경우 발생하는 예외 처리
     * @param e NoHandlerFoundException
     * @return 404 (Not Found)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String,Object>> handleNoHandlerFoundException(NoHandlerFoundException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "요청하신 엔드 포인트를 찾을 수 없습니다. http 메소드와 경로를 다시 확인해주세요.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap.getMap());
    }

    /**
     * 로직 처리에 필요한 데이터가 존재하지 않는 경우 발생하는 예외 처리
     * @param e EntityNotFoundException,
     *          FileNotFoundException,
     *          UsernameNotFoundException
     * @return 404 (Not Found)
     */
    @ExceptionHandler({
            EntityNotFoundException.class,
            FileNotFoundException.class,
            UsernameNotFoundException.class,
            SseEmitterNotFoundException.class
    })
    public ResponseEntity<Map<String,Object>> handleNotFoundException(Exception e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap.getMap());
    }

    /**
     * 중복되는 데이터가 들어왔을 때 발생하는 예외 처리
     * @param e DuplicateDataException
     * @return 409 (Conflict)
     */
    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidInputException(DuplicateDataException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMap.getMap());
    }

    /**
     * 데이터 무결성 위반 예외 처리
     * @param e DataIntegrityViolationException
     * @return 409 (Conflict)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "데이터 무결성을 위반했습니다.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMap.getMap());
    }


    /**
     * 상태가 만료된 경우 발생하는 예외 처리
     * @param e StateExpiredException
     * @return 410 (Gone)
     */
    @ExceptionHandler(StateExpiredException.class)
    public ResponseEntity<Map<String,Object>> handleStateExpiredException(StateExpiredException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(responseMap.getMap());
    }

    /**
     * 파일 전송 예외 처리
     * @param e FileTransferException
     * @return 500 (Internal Server Error)
     */
    @ExceptionHandler(FileTransferException.class)
    public ResponseEntity<Map<String,Object>> handleFileTransferException(FileTransferException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "파일 전송 중 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap.getMap());
    }

    /**
     * SSE 전송 예외 처리
     * @param e SseSendException
     * @return 500 (Internal Server Error)
     */
    @ExceptionHandler(SseSendException.class)
    public ResponseEntity<Map<String,Object>> handleSseSendException(SseSendException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "SSE 전송에 실패했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap.getMap());
    }

    /**
     * 데이터 접근 예외 처리
     * @param e DataAccessException
     * @return 500 (Internal Server Error)
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String,Object>> handleDataAccessException(DataAccessException e){
        // 에러 로그 출력
        e.printStackTrace();

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "데이터에 접근할 수 없습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap.getMap());
    }

    /**
     * 서버에서 발생한 예외 처리
     * @param e Exception
     * @return 500 (Internal Server Error)
     */
    @ExceptionHandler({
            Exception.class
    })
    public ResponseEntity<Map<String,Object>> handleException(Exception e){
        // 에러 로그 출력
        e.printStackTrace();

        // 에러 응답 반환
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap.getMap());
    }
}
