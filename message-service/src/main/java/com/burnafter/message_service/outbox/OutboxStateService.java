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

        if (!retryProperties.enabled() || event.getRetryCount() >= retryProperties.maxAttempts()) {
            event.markDead();
            return true;
        }

        long delay = backoffSeconds(
                retryProperties.baseDelaySeconds(),
                retryProperties.maxDelaySeconds(),
                event.getRetryCount());

        // Jitter spreads retries out so failed events don't all hit the
        // recovering audit service at the same instant.
        long jitter = ThreadLocalRandom.current().nextLong(0, 3);

        event.setNextAttemptAt(Instant.now().plusSeconds(delay + jitter));

        event.setStatus(OutboxEvent.Status.PENDING);
        return false;
    }

    /**
     * Capped exponential backoff: base^attempt seconds, never more than maxDelay.
     * e.g. base=2, max=60 -> 2, 4, 8, 16, 32, 60, 60, ...
     * (Previously base defaulted to 0 and nothing set it, so 0^n = 0: no backoff at all.
     * Uncapped, attempt 19 with base 2 would have waited ~6 days.)
     */
    static long backoffSeconds(int base, int maxDelay, int attempt) {
        long delay = 1;
        for (int i = 0; i < attempt && delay < maxDelay; i++) {
            delay *= base;
        }
        return Math.min(delay, maxDelay);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requeue(UUID eventId) {
        OutboxEvent event = outboxRepository.findById(eventId).orElseThrow();

        event.setStatus(OutboxEvent.Status.PENDING);
        event.setNextAttemptAt(Instant.now().plusSeconds(30)
        );
    }
}