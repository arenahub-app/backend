package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MemberNotBannedException extends DomainException {
    public MemberNotBannedException() {
        super("Membro não está banido de presença", HttpStatus.CONFLICT, "member-not-banned");
    }
}
