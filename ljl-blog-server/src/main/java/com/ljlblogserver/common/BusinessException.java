package com.ljlblogserver.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(ErrorCodes.BAD_REQUEST, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(ErrorCodes.NOT_FOUND, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(ErrorCodes.CONFLICT, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(ErrorCodes.UNAUTHORIZED, message);
    }
}
