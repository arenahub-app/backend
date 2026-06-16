package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyMemberException extends DomainException {
    public UserAlreadyMemberException() {
        super("Usuário já é membro deste grupo", HttpStatus.CONFLICT, "user-already-member");
    }
}
