package applesquare.moment.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
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
     * 로직 처리에 필요한 데이터가 존재하지 않는 경우 발생하는 예외 처리
     * @param e EntityNotFoundException,
     *          UsernameNotFoundException
     * @return 404 (Not Found)
     */
    @ExceptionHandler({
            EntityNotFoundException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<Map<String,Object>> handleNoSuchElementException(Exception e){
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap.getMap());
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
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "서버에서 예상치 못한 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap.getMap());
    }
}
