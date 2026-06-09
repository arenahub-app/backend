package com.arenahub.application.auth.dto;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds
) {}
