package com.burnafter.message_service.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

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

    private UUID aggregateId;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int retryCount;

    private Instant nextAttemptAt;

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

    public void incrementRetryWithBackoff() {
        retryCount++;

        if (retryCount >= 5) {
            markDead();
            return;
        }

        long backoffSeconds = (long) Math.pow(2, retryCount);
        nextAttemptAt = Instant.now().plusSeconds(backoffSeconds);
        status = Status.PENDING;
    }

    public void setStatus(Status status) {
        this.status = status;
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
