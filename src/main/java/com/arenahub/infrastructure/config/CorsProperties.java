package com.arenahub.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "arenahub.cors")
public record CorsProperties(String allowedOrigins) {

    public List<String> allowedOriginsList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList();
    }
}
