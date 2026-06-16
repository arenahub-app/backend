package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class NotAMemberException extends DomainException {
    public NotAMemberException() {
        super("Você não é membro deste grupo", HttpStatus.FORBIDDEN, "not-a-member");
    }
}
