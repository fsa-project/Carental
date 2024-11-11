package vn.fsaproject.carental.exception;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {
    UNKNOWN_EXCEPTION(1001,"Uncategorize Exception"),
    USER_NOT_FOUND(1002,"User not found "),
    UNAUTHENTICATED(1004,"Your password are incorrect")

    ;
    private int code;
    private String message;
}
