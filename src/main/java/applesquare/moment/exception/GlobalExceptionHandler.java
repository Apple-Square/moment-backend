package applesquare.moment.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        BindingResult bindingResult=e.getBindingResult();
        ResponseMap responseMap=new ResponseMap();

        final List<String> errorMessages=new LinkedList<>();
        bindingResult.getFieldErrors().forEach((fieldError)->{
            errorMessages.add(fieldError.getField()+": "+fieldError.getDefaultMessage()+".");
        });
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
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(responseMap.getMap());
    }

    /**
     * 접근 거부 예외 처리
     * @param e AccessDeniedException
     * @return 403 (Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException e){
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
            UsernameNotFoundException.class
    })
    public ResponseEntity<Map<String,Object>> handleNotFoundException(Exception e){
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
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "데이터 무결성을 위반했습니다.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMap.getMap());
    }

    /**
     * 파일 전송 예외 처리
     * @param e FileTransferException
     * @return 500 (Internal Server Error)
     */
    @ExceptionHandler(FileTransferException.class)
    public ResponseEntity<Map<String,Object>> handleFileTransferException(FileTransferException e){
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "파일 전송 중 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap.getMap());
    }

    /**
     * 데이터 접근 예외 처리
     * @param e DataAccessException
     * @return 500 (Internal Server Error)
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String,Object>> handleDataAccessException(DataAccessException e){
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "데이터에 접근할 수 없습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap.getMap());
    }

    /**
     * 서버에서 발생한 예외 처리
     * @param e Exception
     * @return 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleException(Exception e){
        log.error(e.getMessage());

        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "서버에서 예상치 못한 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap.getMap());
    }
}
