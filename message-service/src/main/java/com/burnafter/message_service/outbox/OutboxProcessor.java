package com.burnafter.message_service.outbox;

import com.burnafter.message_service.repository.OutboxRepository;
import com.burnafter.message_service.service.AuditDeliveryService;
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

    @Value("${app.instance-id}")
    private String instanceId;

    public OutboxProcessor(OutboxClaimService claimService,
                           AuditDeliveryService deliveryService, OutboxRepository outboxRepository) {
        this.claimService = claimService;
        this.deliveryService = deliveryService;
        this.outboxRepository = outboxRepository;
    }

    public void processBatch(int batchSize) {

        List<OutboxEvent> events =
                claimService.claimBatch(batchSize);

        for (OutboxEvent event : events) {

            try {
                deliveryService.deliver(event);
                updateSuccess(event.getId());
            } catch (Exception ex) {
                updateFailure(event.getId(), ex);
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
    public void updateFailure(UUID eventId, Exception ex) {
        OutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow();
        event.incrementRetryWithBackoff(ex);
    }
}
