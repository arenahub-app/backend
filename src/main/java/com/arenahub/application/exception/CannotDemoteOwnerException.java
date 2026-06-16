package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class CannotDemoteOwnerException extends DomainException {
    public CannotDemoteOwnerException() {
        super("O OWNER não pode ser rebaixado sem transferir o papel a outro membro",
                HttpStatus.UNPROCESSABLE_ENTITY, "cannot-demote-owner");
    }
}
