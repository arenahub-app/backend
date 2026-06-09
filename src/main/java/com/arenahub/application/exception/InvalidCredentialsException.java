package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Credenciais inválidas", HttpStatus.UNAUTHORIZED, "invalid-credentials");
    }
}
