package vn.fsaproject.carental.exception;


import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import vn.fsaproject.carental.dto.response.RestResponse;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandle {

    @ExceptionHandler(value = RuntimeException.class)
    @ResponseBody
    ResponseEntity<RestResponse<Object>> runtimeExceptionHandler(RuntimeException e) {
        RestResponse<Object> restResponse = new RestResponse<Object>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setMessage(e.getMessage());
        return ResponseEntity.badRequest().body(restResponse);
    }

    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            BadCredentialsException.class
    })
    @ResponseBody
    ResponseEntity<RestResponse<Object>> handleIdException(Exception e) {
        RestResponse<Object> restResponse = new RestResponse<Object>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError(e.getMessage());
        restResponse.setMessage("Exception occurs...");
        return ResponseEntity.badRequest().body(restResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    ResponseEntity<RestResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        RestResponse<Object> restResponse = new RestResponse<Object>();
        restResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        restResponse.setError(e.getBody().getDetail());

        List<String> errors = fieldErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();

        restResponse.setMessage(errors.size() > 1 ? errors.toString() : errors.get(0));

        return ResponseEntity.badRequest().body(restResponse);
    }

    @ExceptionHandler(value = PermissionException.class)
    @ResponseBody
    public ResponseEntity<RestResponse<Object>> handlePermissionException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setMessage("Forbidden");
        res.setError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    public ResponseEntity<RestResponse<Object>> handleNotFountException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError(ex.getMessage());
        res.setMessage("404 Not Found, URL may not exist...");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);

    }

}