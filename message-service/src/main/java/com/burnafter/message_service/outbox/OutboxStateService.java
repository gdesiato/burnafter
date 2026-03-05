package com.burnafter.message_service.outbox;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxStateService {

    private final OutboxRepository outboxRepository;

    public OutboxStateService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSuccess(UUID eventId) {
        OutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow();
        event.markProcessed();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateFailure(UUID eventId, Exception ex) {
        OutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow();
        event.incrementRetryWithBackoff(ex);
        return event.isDead();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requeue(UUID eventId) {
        OutboxEvent event = outboxRepository.findById(eventId)
                .orElseThrow();
        event.setStatus(OutboxEvent.Status.PENDING);
        event.setNextAttemptAt(Instant.now().plusSeconds(30));
    }
}
