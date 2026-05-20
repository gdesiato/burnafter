package com.burnafter.message_service.outbox;

import com.burnafter.message_service.configuration.RetryProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OutboxStateService {

    private final OutboxRepository outboxRepository;
    private final RetryProperties retryProperties;

    public OutboxStateService(
            OutboxRepository outboxRepository,
            RetryProperties retryProperties) {

        this.outboxRepository = outboxRepository;
        this.retryProperties = retryProperties;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSuccess(UUID eventId) {

        OutboxEvent event = outboxRepository.findById(eventId).orElseThrow();
        event.markProcessed();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateFailure(UUID eventId, Exception ex) {

        OutboxEvent event = outboxRepository.findById(eventId).orElseThrow();

        event.incrementRetry();
        event.setLastError(ex.getMessage());

        if (!retryProperties.isEnabled() || event.getRetryCount() >= retryProperties.getMaxAttempts()) {
            event.markDead();
            return true;
        }

        long delay = (long) Math.pow(
                retryProperties.getBaseDelaySeconds(),
                event.getRetryCount());

        long jitter = ThreadLocalRandom.current().nextLong(0, 3);

        event.setNextAttemptAt(Instant.now().plusSeconds(delay + jitter));

        event.setStatus(OutboxEvent.Status.PENDING);
        return false;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requeue(UUID eventId) {
        OutboxEvent event = outboxRepository.findById(eventId).orElseThrow();

        event.setStatus(OutboxEvent.Status.PENDING);
        event.setNextAttemptAt(Instant.now().plusSeconds(30)
        );
    }
}