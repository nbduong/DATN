package com.zawser.DATN.exception;

public enum ErrorCode {
    USER_EXISTED(1001, "User already existed"),
    UNCATEGORIZED_EXCEPTION(9999, "UNCATEGORIZED ERROR"),
    INVALID_USERNAME(1002, "Username must be at least 3 characters long"),
    INVALID_PASSWORD(1003, "Password must be at least 8 characters long"),
    INVALID_KEY_EXCEPTION(1004, "Invalid key"),
    USER_NOT_FOUND(1005, "User not found"),

    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
