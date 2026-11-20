package com.burnafter.message_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final Counter messagesCreated;

    public MetricsService(MeterRegistry registry) {
        // Prometheus exposes this as messages_total (the "_created" suffix is reserved there).
        this.messagesCreated = Counter.builder("messages_created_total")
                .description("Total messages created")
                .register(registry);
    }

    public void incrementMessagesCreated() {
        messagesCreated.increment();
    }
}
