package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyRegisteredException extends DomainException {

    public EmailAlreadyRegisteredException(String email) {
        super("Email já cadastrado: " + email, HttpStatus.CONFLICT, "email-already-registered");
    }
}
