package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MatchNotFoundException extends DomainException {
    public MatchNotFoundException() {
        super("Partida não encontrada", HttpStatus.NOT_FOUND, "match-not-found");
    }
}
