package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class ListAlreadyClosedException extends DomainException {
    public ListAlreadyClosedException() {
        super("A lista de presença desta partida já foi encerrada",
                HttpStatus.UNPROCESSABLE_ENTITY, "list-already-closed");
    }
}
