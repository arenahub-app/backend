package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class PlayerAlreadyInTeamException extends DomainException {
    public PlayerAlreadyInTeamException() {
        super("Jogador já está neste time", HttpStatus.BAD_REQUEST, "player-already-in-team");
    }
}
