package com.burnafter.message_service.outbox;

import com.burnafter.message_service.repository.OutboxRepository;
import com.burnafter.message_service.service.AuditDeliveryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxProcessor {

    private final OutboxRepository repository;
    private final AuditDeliveryService deliveryService;

    public OutboxProcessor(OutboxRepository repository,
                           AuditDeliveryService deliveryService) {
        this.repository = repository;
        this.deliveryService = deliveryService;
    }

    @Transactional
    public List<OutboxEvent> claimBatch(int batchSize) {

        List<OutboxEvent> events =
                repository.claimBatch(Instant.now(), batchSize);

        events.forEach(e -> e.setStatus(OutboxEvent.Status.PROCESSING));

        return events;
    }

    public void processBatch(int batchSize) {

        List<OutboxEvent> events = claimBatch(batchSize);

        for (OutboxEvent event : events) {
            deliveryService.deliver(event);
        }
    }
}
