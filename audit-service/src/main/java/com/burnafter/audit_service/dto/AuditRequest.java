package com.burnafter.audit_service.dto;

public record AuditRequest(
        String eventId,
        String aggregateId,
        String eventType,
        Long timestamp
) {}
