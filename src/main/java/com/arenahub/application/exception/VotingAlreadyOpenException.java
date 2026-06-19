package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class VotingAlreadyOpenException extends DomainException {
    public VotingAlreadyOpenException() {
        super("Já existe uma votação ativa para este grupo", HttpStatus.CONFLICT, "voting-already-open");
    }
}
