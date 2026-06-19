package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class VotingBanNotFoundException extends DomainException {
    public VotingBanNotFoundException() {
        super("Banimento de votação não encontrado", HttpStatus.NOT_FOUND, "voting-ban-not-found");
    }
}
