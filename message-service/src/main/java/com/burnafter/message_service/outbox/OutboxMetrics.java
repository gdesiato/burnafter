package com.burnafter.message_service.outbox;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public class OutboxMetrics {

    public OutboxMetrics(MeterRegistry registry, OutboxRepository repository) {
        Gauge.builder("outbox_queue_size",
                        repository,
                        repo -> repo.countByStatus(OutboxEvent.Status.PENDING))
                .description("Number of events waiting in outbox")
                .register(registry);
    }
}
