package com.arenahub.application.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends DomainException {

    public InvalidRefreshTokenException() {
        super("Refresh token inválido ou expirado", HttpStatus.UNAUTHORIZED, "refresh-token-invalid");
    }
}
