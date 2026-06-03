package com.burnafter.audit_service.dto;

import java.time.Instant;

public record AuditRequest(
        String eventId,
        String aggregateId,
        String eventType,
        Instant outboxCreatedAt
) {}
