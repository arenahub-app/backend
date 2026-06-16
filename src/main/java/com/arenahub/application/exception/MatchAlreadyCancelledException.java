package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MatchAlreadyCancelledException extends DomainException {
    public MatchAlreadyCancelledException() {
        super("Partida já está cancelada", HttpStatus.UNPROCESSABLE_ENTITY, "match-already-cancelled");
    }
}
