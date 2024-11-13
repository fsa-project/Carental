package vn.fsaproject.carental.exception;

import org.springframework.security.authentication.AuthenticationServiceException;
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
    @ExceptionHandler(value = AuthenticationServiceException.class)
    ResponseEntity<ApiResponse>handleAuthenticationServiceException(AuthenticationServiceException e){
        ApiResponse response = new ApiResponse<>();
        response.setCode(ErrorCode.EXPIRED_TOKEN.getCode());
        response.setMessage(ErrorCode.EXPIRED_TOKEN.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse>hendleAppExeption(AppException e){
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse response = new ApiResponse();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
