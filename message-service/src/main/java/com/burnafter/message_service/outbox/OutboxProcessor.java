package com.burnafter.message_service.outbox;

import com.burnafter.message_service.service.AuditDeliveryService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxClaimService claimService;
    private final AuditDeliveryService deliveryService;
    private final OutboxRepository outboxRepository;
    private final Counter processedCounter;
    private final Counter retryCounter;
    private final Counter deadCounter;
    private final Timer deliveryTimer;

    @Value("${app.instance-id}")
    private String instanceId;

    public OutboxProcessor(OutboxClaimService claimService,
                           AuditDeliveryService deliveryService,
                           OutboxRepository outboxRepository,
                           MeterRegistry meterRegistry) {

        this.claimService = claimService;
        this.deliveryService = deliveryService;
        this.outboxRepository = outboxRepository;

        this.processedCounter = meterRegistry.counter("outbox.events.processed");
        this.retryCounter = meterRegistry.counter("outbox.events.retry");
        this.deadCounter = meterRegistry.counter("outbox.events.dead");
        this.deliveryTimer = meterRegistry.timer("outbox.delivery.duration");
    }

    public void processBatch(int batchSize) {
        List<OutboxEvent> events = claimService.claimBatch(batchSize);
        for (OutboxEvent event : events) {
            try {
                deliveryTimer.record(() -> deliveryService.deliver(event));

                updateSuccess(event.getId());
                processedCounter.increment();

            } catch (Exception ex) {
                boolean isDead = updateFailure(event.getId(), ex);
                retryCounter.increment();

                if (isDead) deadCounter.increment();
            }
        }
    }

    @Transactional
    public void updateSuccess(UUID eventId) {
        OutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow();
        event.markProcessed();
    }

    @Transactional
    public boolean updateFailure(UUID eventId, Exception ex) {
        OutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow();
        event.incrementRetryWithBackoff(ex);
        return event.isDead();
    }
}
