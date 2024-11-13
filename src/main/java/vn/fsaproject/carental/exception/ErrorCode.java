package vn.fsaproject.carental.exception;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {
    UNKNOWN_EXCEPTION(1001,"Uncategorize Exception"),
    USER_NOT_FOUND(1002,"User not found "),
    UNAUTHENTICATED(1004,"Your password are incorrect"),
    ROLE_NOT_FOUND(1005,"Your role does not exist"),
    EXPIRED_TOKEN(1006,"Token expired")
    ;
    private int code;
    private String message;
}
