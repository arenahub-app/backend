package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class GroupInactiveException extends DomainException {
    public GroupInactiveException() {
        super("Este grupo está desativado", HttpStatus.UNPROCESSABLE_ENTITY, "group-inactive");
    }
}
