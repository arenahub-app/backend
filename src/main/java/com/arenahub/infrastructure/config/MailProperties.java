package com.arenahub.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenahub.mail")
public record MailProperties(String from, String apiKey) {}
