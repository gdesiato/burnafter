package com.burnafter.burnafter.outbox;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxEvent.class);

    private static final int MAX_RETRIES = 10;

    public enum Status {
        PENDING,
        PROCESSED,
        FAILED,
        DEAD
    }

    @Id
    private UUID id;

    private UUID aggregateId;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Instant createdAt;

    private Instant nextAttemptAt;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column
    private Instant firstAttemptAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private int retryCount;

    protected OutboxEvent() {}

    public OutboxEvent(UUID aggregateId, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.nextAttemptAt = Instant.now();
        this.status = Status.PENDING;
        this.retryCount = 0;
    }

    public void markProcessed() {
        this.status = Status.PROCESSED;
    }

    public void markFailed() {
        this.status = Status.FAILED;
    }

    public void incrementRetryWithBackoff() {
        this.retryCount++;

        // Set firstAttemptAt on first failure
        if (this.firstAttemptAt == null) {
            this.firstAttemptAt = Instant.now();
        }

        // Age-based circuit breaker
        Duration age = Duration.between(firstAttemptAt, Instant.now());
        if (age.toHours() > 24) {
            this.status = Status.DEAD;
            return;
        }

        // Retry count circuit breaker
        if (this.retryCount >= 10) {
            this.status = Status.DEAD;
            return;
        }

        // Exponential backoff, capped at 10 minutes
        long delaySeconds = Math.min((long) Math.pow(2, retryCount), 600);
        this.nextAttemptAt = Instant.now().plusSeconds(delaySeconds);
    }

    public void recordFailure(String errorMessage) {
        this.lastError = errorMessage;
        incrementRetryWithBackoff();
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }
}

