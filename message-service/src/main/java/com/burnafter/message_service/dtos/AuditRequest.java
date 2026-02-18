package com.burnafter.message_service.dtos;

import java.time.Instant;

public record AuditRequest(
        String eventId,
        String aggregateId,
        String eventType,
        Instant occurredAt,
        String sourceService
) {}
