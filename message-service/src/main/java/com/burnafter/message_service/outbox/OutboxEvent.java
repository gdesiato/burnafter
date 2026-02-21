package com.burnafter.message_service.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    public enum Status {
        PENDING,
        PROCESSING,
        PROCESSED,
        DEAD
    }

    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    private UUID aggregateId;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    protected OutboxEvent() {}

    public OutboxEvent(UUID aggregateId, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.status = Status.PENDING;
        this.retryCount = 0;
        this.nextAttemptAt = Instant.now();
    }

    public void markProcessing() {
        this.status = Status.PROCESSING;
    }

    public void markProcessed() {
        this.status = Status.PROCESSED;
    }

    public void markDead() {
        this.status = Status.DEAD;
    }

    public void incrementRetryWithBackoff(Exception ex) {
        retryCount++;
        this.lastError = ex.getMessage();

        if (retryCount >= 5) {
            markDead();
            return;
        }

        long baseDelay = (long) Math.pow(2, retryCount);
        long jitter = ThreadLocalRandom.current().nextLong(0, 3);
        nextAttemptAt = Instant.now().plusSeconds(baseDelay + jitter);
        status = Status.PENDING;
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
}
