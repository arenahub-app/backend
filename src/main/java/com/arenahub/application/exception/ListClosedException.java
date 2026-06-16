package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class ListClosedException extends DomainException {
    public ListClosedException() {
        super("A lista de presença desta partida já foi encerrada",
                HttpStatus.UNPROCESSABLE_ENTITY, "list-closed");
    }
}
