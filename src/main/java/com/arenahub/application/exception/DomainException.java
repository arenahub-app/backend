package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public abstract class DomainException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected DomainException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getErrorCode() { return errorCode; }
}
