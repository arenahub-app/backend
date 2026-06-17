package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class PlayerNotInFormationException extends DomainException {
    public PlayerNotInFormationException() {
        super("Jogador não encontrado na formação", HttpStatus.NOT_FOUND, "player-not-in-formation");
    }
}
