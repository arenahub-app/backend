package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class InviteNotFoundException extends DomainException {
    public InviteNotFoundException() {
        super("Convite não encontrado", HttpStatus.NOT_FOUND, "invite-not-found");
    }
}
