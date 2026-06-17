package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends DomainException {
    public InvalidFileTypeException() {
        super("Tipo de arquivo inválido. Aceitos: JPEG, PNG, PDF",
                HttpStatus.UNPROCESSABLE_ENTITY, "invalid-file-type");
    }
}
