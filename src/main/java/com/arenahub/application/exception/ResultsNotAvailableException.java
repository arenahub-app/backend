package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class ResultsNotAvailableException extends DomainException {
    public ResultsNotAvailableException() {
        super("Resultados parciais só são visíveis para administradores durante a votação", HttpStatus.FORBIDDEN, "results-not-available");
    }
}
