package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class AttemptNotFoundException extends DomainException {
    public AttemptNotFoundException() {
        super("Tentativa de pagamento não encontrada", HttpStatus.NOT_FOUND, "attempt-not-found");
    }
}
