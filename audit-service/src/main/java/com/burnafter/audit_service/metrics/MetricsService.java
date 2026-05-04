package com.burnafter.audit_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final Counter messagesConsumed;

    public MetricsService(MeterRegistry registry) {
        this.messagesConsumed = Counter.builder("messages_consumed_total")
                .description("Total messages consumed by audit service")
                .register(registry);
    }

    public void incrementMessagesConsumed() {
        messagesConsumed.increment();
    }
}
