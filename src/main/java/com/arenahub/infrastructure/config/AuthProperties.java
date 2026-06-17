package com.arenahub.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenahub.auth")
public record AuthProperties(boolean emailVerificationEnabled) {}
