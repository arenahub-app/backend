package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class MemberAlreadyBannedFromVotingException extends DomainException {
    public MemberAlreadyBannedFromVotingException() {
        super("Membro já está banido desta votação", HttpStatus.CONFLICT, "member-already-banned-from-voting");
    }
}
