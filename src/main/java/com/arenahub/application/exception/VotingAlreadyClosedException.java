package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class VotingAlreadyClosedException extends DomainException {
    public VotingAlreadyClosedException() {
        super("Votação já está encerrada", HttpStatus.UNPROCESSABLE_ENTITY, "voting-already-closed");
    }
}
