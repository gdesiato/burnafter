package com.burnafter.message_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * outbox.retry.* - set per experiment profile (application-retry*.yml).
 * Retry is off unless a profile enables it.
 */
@ConfigurationProperties(prefix = "outbox.retry")
public record RetryProperties(
        boolean enabled,
        int maxAttempts,
        @DefaultValue("2") int baseDelaySeconds,
        @DefaultValue("60") int maxDelaySeconds) {
}
