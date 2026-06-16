package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MemberAlreadyBannedException extends DomainException {
    public MemberAlreadyBannedException() {
        super("Membro já está banido de presença", HttpStatus.CONFLICT, "member-already-banned");
    }
}
