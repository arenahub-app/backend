package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class FormationNotFoundException extends DomainException {
    public FormationNotFoundException() {
        super("Formação não encontrada", HttpStatus.NOT_FOUND, "formation-not-found");
    }
}
