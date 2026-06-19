package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class VotingNotFoundException extends DomainException {
    public VotingNotFoundException() {
        super("Votação não encontrada", HttpStatus.NOT_FOUND, "voting-not-found");
    }
}
