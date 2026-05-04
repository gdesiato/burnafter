package com.burnafter.message_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final Counter messagesCreated;
    private final Counter outboxProcessed;
    private final Counter outboxFailures;

    public MetricsService(MeterRegistry registry) {
        this.messagesCreated = Counter.builder("messages_created_total")
                .description("Total messages created")
                .register(registry);

        this.outboxProcessed = Counter.builder("outbox_processed_total")
                .description("Total outbox events processed")
                .register(registry);

        this.outboxFailures = Counter.builder("outbox_failures_total")
                .description("Total outbox processing failures")
                .register(registry);
    }

    public void incrementMessagesCreated() {
        messagesCreated.increment();
    }

    public void incrementOutboxProcessed() {
        outboxProcessed.increment();
    }

    public void incrementOutboxFailures() {
        outboxFailures.increment();
    }
}