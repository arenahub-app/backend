package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class TeamNotInFormationException extends DomainException {
    public TeamNotInFormationException() {
        super("Time destino não pertence à formação", HttpStatus.NOT_FOUND, "team-not-in-formation");
    }
}
