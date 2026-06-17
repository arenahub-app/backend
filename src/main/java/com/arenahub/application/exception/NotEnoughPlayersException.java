package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class NotEnoughPlayersException extends DomainException {
    public NotEnoughPlayersException() {
        super("Jogadores insuficientes para formar os times", HttpStatus.UNPROCESSABLE_ENTITY, "not-enough-players");
    }
}
