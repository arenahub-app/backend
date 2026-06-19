package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class VoterIsBannedException extends DomainException {
    public VoterIsBannedException() {
        super("Você está banido desta votação e não pode votar", HttpStatus.FORBIDDEN, "voter-is-banned");
    }
}
