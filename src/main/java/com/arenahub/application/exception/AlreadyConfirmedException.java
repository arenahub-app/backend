package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class AlreadyConfirmedException extends DomainException {
    public AlreadyConfirmedException() {
        super("Você já confirmou presença nesta partida", HttpStatus.CONFLICT, "already-confirmed");
    }
}
