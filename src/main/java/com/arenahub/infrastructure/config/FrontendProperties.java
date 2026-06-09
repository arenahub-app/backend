package com.arenahub.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenahub.frontend")
public record FrontendProperties(String baseUrl) {}
