package com.burnafter.message_service.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * resilience4j-spring-boot3's metrics auto-configuration does not activate on
 * Spring Boot 4, so circuit breaker metrics (state, failure rate, calls) are
 * bound to Micrometer explicitly. Exposes e.g. resilience4j_circuitbreaker_state.
 */
@Configuration
public class ResilienceMetricsConfig {

    public ResilienceMetricsConfig(CircuitBreakerRegistry circuitBreakerRegistry,
                                   MeterRegistry meterRegistry) {
        TaggedCircuitBreakerMetrics
                .ofCircuitBreakerRegistry(circuitBreakerRegistry)
                .bindTo(meterRegistry);
    }
}
