package com.burnafter.burnafter.dtos;

public record AuditRequest(
        String messageId,
        String action,
        long timestamp
) {}
