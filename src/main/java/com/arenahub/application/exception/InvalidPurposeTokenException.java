package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class InvalidPurposeTokenException extends DomainException {

    public InvalidPurposeTokenException(String detail) {
        super(detail, HttpStatus.BAD_REQUEST, "invalid-token");
    }
}
