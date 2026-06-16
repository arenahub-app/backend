package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class RefereeCannotConfirmException extends DomainException {
    public RefereeCannotConfirmException() {
        super("Árbitros não podem confirmar presença como jogadores",
                HttpStatus.FORBIDDEN, "referee-cannot-confirm");
    }
}
