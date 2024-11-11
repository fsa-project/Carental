package vn.fsaproject.carental.exception;

import vn.fsaproject.carental.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
@ControllerAdvice
public class ExeptionHandler {
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse>handleRuntimeExeption(RuntimeException e){
        ApiResponse response = new ApiResponse<>();
        response.setCode(ErrorCode.UNKNOWN_EXCEPTION.getCode());
        response.setMessage(ErrorCode.UNKNOWN_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
