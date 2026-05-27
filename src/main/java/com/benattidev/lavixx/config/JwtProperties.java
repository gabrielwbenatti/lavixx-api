package com.benattidev.lavixx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        int expirationMinutes,
        String issuer) {
}
