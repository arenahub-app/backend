package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class InsufficientRoleException extends DomainException {
    public InsufficientRoleException() {
        super("Permissão insuficiente para esta operação", HttpStatus.FORBIDDEN, "insufficient-role");
    }
}
