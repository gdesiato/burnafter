package com.burnafter.audit_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {
    private final Counter messagesConsumed;
    private final Counter duplicateAuditCounter;

    public MetricsService(MeterRegistry registry) {

        this.messagesConsumed = Counter.builder("messages_consumed_total")
                .description("Total messages consumed by audit service")
                .register(registry);

        this.duplicateAuditCounter =
                Counter.builder("consistency.audit.duplicate")
                        .description("Duplicate audit events detected")
                        .register(registry);
    }

    public void incrementMessagesConsumed() { messagesConsumed.increment(); }
    public void incrementDuplicateAudit() { duplicateAuditCounter.increment(); }
}
