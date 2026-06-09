package com.arenahub.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenahub.jwt")
public record JwtProperties(
        String secret,
        int accessTokenExpirationMinutes,
        int refreshTokenExpirationDays
) {}
