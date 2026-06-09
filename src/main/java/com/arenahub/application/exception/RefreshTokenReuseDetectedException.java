package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class RefreshTokenReuseDetectedException extends DomainException {

    public RefreshTokenReuseDetectedException() {
        super("Reutilização de refresh token detectada — sessão encerrada",
                HttpStatus.UNAUTHORIZED, "refresh-token-reuse-detected");
    }
}
