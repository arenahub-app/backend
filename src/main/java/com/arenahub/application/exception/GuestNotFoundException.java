package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class GuestNotFoundException extends DomainException {
    public GuestNotFoundException() {
        super("Convidado não encontrado", HttpStatus.NOT_FOUND, "guest-not-found");
    }
}
