package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MatchInPastException extends DomainException {
    public MatchInPastException() {
        super("A data da partida deve ser pelo menos 30 minutos no futuro",
                HttpStatus.BAD_REQUEST, "match-in-past");
    }
}
