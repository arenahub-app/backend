package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class ChargeNotPendingException extends DomainException {
    public ChargeNotPendingException() {
        super("Cobrança não está pendente", HttpStatus.UNPROCESSABLE_ENTITY, "charge-not-pending");
    }
}
