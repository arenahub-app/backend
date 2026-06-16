package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class GroupNotFoundException extends DomainException {
    public GroupNotFoundException() {
        super("Grupo não encontrado", HttpStatus.NOT_FOUND, "group-not-found");
    }
}
