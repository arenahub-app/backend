package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class CannotRemoveLastOwnerException extends DomainException {
    public CannotRemoveLastOwnerException() {
        super("Não é possível remover o único OWNER do grupo",
                HttpStatus.UNPROCESSABLE_ENTITY, "cannot-remove-last-owner");
    }
}
