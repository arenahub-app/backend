package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class InviteExpiredException extends DomainException {
    public InviteExpiredException() {
        super("Convite expirado ou com uso máximo atingido", HttpStatus.GONE, "invite-expired");
    }
}
