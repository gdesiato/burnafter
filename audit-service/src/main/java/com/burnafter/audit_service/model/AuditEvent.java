package com.burnafter.audit_service.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "audit_events",
        uniqueConstraints = @UniqueConstraint(columnNames = "eventId"))
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private Instant createdAt;

    protected AuditEvent() {}

    public AuditEvent(String eventId,
                      String aggregateId,
                      String eventType) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.createdAt = Instant.now();
    }
}
