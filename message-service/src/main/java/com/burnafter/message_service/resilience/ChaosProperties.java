package com.burnafter.message_service.resilience;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * resilience.chaos.* - set by the "chaos" profile (application-chaos.yml).
 * Off unless enabled.
 *
 * @param failureRate share of deliveries that fail immediately (0.15 = 15%)
 * @param delayRate   share of deliveries delayed by delayMs before being sent
 */
@ConfigurationProperties(prefix = "resilience.chaos")
public record ChaosProperties(boolean enabled, double failureRate, double delayRate, long delayMs) {
}
