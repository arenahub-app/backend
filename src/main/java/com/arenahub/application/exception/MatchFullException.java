package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MatchFullException extends DomainException {
    public MatchFullException() {
        super("Partida está cheia", HttpStatus.UNPROCESSABLE_ENTITY, "match-full");
    }
}
