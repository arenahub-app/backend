package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends DomainException {

    public EmailNotVerifiedException() {
        super("Email não verificado", HttpStatus.FORBIDDEN, "email-not-verified");
    }
}
