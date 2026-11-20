package com.burnafter.audit_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

// Size limits mirror the audit_events column widths, so a bad request is
// rejected with 400 instead of failing at the database with 500.
public record AuditRequest(
        @NotBlank @Size(max = 100) String eventId,
        @NotBlank @Size(max = 100) String aggregateId,
        @NotBlank @Size(max = 50) String eventType,
        @NotNull Instant outboxCreatedAt
) {}
