package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends DomainException {
    public MemberNotFoundException() {
        super("Membro não encontrado no grupo", HttpStatus.NOT_FOUND, "member-not-found");
    }
}
