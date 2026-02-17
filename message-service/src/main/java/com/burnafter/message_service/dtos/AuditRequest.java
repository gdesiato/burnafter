package com.burnafter.message_service.dtos;

public record AuditRequest(
        String eventId,
        String aggregateId,
        String eventType,
        long timestamp
) {}
