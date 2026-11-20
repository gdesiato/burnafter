package com.burnafter.audit_service.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Read-side mapping of audit_events. Rows are written only through
 * AuditEventRepository.insertIfAbsent (idempotent native insert), and the
 * schema is owned by Flyway; this entity exists so Hibernate can validate it.
 */
@Entity
@Table(name = "audit_events")
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
    private Instant outboxCreatedAt;

    @Column(nullable = false)
    private Instant consumedAt;

    protected AuditEvent() {}

    public String getEventId() {
        return eventId;
    }
}
