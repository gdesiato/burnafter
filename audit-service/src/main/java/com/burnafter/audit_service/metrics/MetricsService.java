package com.burnafter.audit_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MetricsService {

    private final Counter messagesConsumed;
    private final Counter duplicateAuditCounter;
    private final Timer divergenceWindow;

    public MetricsService(MeterRegistry registry) {
        this.messagesConsumed = Counter.builder("audit.messages.consumed")
                .description("Audit events stored (first deliveries only)")
                .register(registry);

        this.duplicateAuditCounter = Counter.builder("consistency.audit.duplicate")
                .description("Duplicate audit events detected")
                .register(registry);

        this.divergenceWindow = Timer.builder("consistency.divergence.window")
                .description("Time between outbox creation and audit persistence")
                .register(registry);
    }

    public void incrementMessagesConsumed() {
        messagesConsumed.increment();
    }

    public void incrementDuplicateAudit() {
        duplicateAuditCounter.increment();
    }

    /** Negative lag can only come from clock skew between services; clamp it to zero. */
    public void recordDivergence(Duration lag) {
        divergenceWindow.record(lag.isNegative() ? Duration.ZERO : lag);
    }
}
