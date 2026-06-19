package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class VotingClosedException extends DomainException {
    public VotingClosedException() {
        super("Esta votação já foi encerrada", HttpStatus.UNPROCESSABLE_ENTITY, "voting-closed");
    }
}
