package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class AccountInactiveException extends DomainException {

    public AccountInactiveException() {
        super("Conta inativa", HttpStatus.FORBIDDEN, "account-inactive");
    }
}
