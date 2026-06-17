package com.arenahub.presentation.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Boolean emailVerificationRequired
) {
    public AuthResponse(String accessToken, long expiresIn) {
        this(accessToken, "Bearer", expiresIn, null);
    }

    public AuthResponse(String accessToken, long expiresIn, boolean emailVerificationRequired) {
        this(accessToken, "Bearer", expiresIn, emailVerificationRequired);
    }
}
