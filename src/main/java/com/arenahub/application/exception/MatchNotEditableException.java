package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MatchNotEditableException extends DomainException {
    public MatchNotEditableException() {
        super("Partida não pode ser editada — lista fechada ou partida cancelada",
                HttpStatus.UNPROCESSABLE_ENTITY, "match-not-editable");
    }
}
