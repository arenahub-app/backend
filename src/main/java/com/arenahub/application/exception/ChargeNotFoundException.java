package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class ChargeNotFoundException extends DomainException {
    public ChargeNotFoundException() {
        super("Cobrança não encontrada", HttpStatus.NOT_FOUND, "charge-not-found");
    }
}
