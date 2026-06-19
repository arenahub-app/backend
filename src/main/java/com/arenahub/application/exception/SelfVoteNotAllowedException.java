package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class SelfVoteNotAllowedException extends DomainException {
    public SelfVoteNotAllowedException() {
        super("Não é permitido votar em si mesmo", HttpStatus.UNPROCESSABLE_ENTITY, "self-vote-not-allowed");
    }
}
