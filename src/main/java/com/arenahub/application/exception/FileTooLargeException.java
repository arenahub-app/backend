package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class FileTooLargeException extends DomainException {
    public FileTooLargeException() {
        super("Arquivo excede o tamanho máximo de 10 MB",
                HttpStatus.UNPROCESSABLE_ENTITY, "file-too-large");
    }
}
