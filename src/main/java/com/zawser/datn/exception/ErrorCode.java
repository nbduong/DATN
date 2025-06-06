package com.zawser.datn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "UNCATEGORIZED ERROR", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1001, "User already existed", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1002, "Username must be at least {min} characters long", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1003, "Password must be at least {min} characters long", HttpStatus.BAD_REQUEST),
    INVALID_KEY_EXCEPTION(1004, "Invalid key", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005, "User not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED_EXCEPTION(1006, "Unauthenticated Error", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_EXCEPTION(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code = 1905;
    private String message;
    private HttpStatusCode statusCode;
}
