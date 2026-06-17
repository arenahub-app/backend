package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class PresenceListNotClosedException extends DomainException {
    public PresenceListNotClosedException() {
        super("Lista de presença ainda não foi fechada", HttpStatus.UNPROCESSABLE_ENTITY, "presence-list-not-closed");
    }
}
