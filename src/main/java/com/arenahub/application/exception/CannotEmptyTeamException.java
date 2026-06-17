package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class CannotEmptyTeamException extends DomainException {
    public CannotEmptyTeamException() {
        super("Não é possível remover o único jogador de um time", HttpStatus.UNPROCESSABLE_ENTITY, "cannot-empty-team");
    }
}
