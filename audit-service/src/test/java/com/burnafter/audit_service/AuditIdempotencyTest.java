package com.burnafter.audit_service;

import com.burnafter.audit_service.controller.AuditController;
import com.burnafter.audit_service.dto.AuditRequest;
import com.burnafter.audit_service.repository.AuditEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditIdempotencyTest {

    @Autowired AuditController controller;
    @Autowired AuditEventRepository repository;
    @Autowired MeterRegistry registry;

    @Test
    void duplicateDelivery_isAcknowledgedAndStoredOnce() {
        String eventId = UUID.randomUUID().toString();
        AuditRequest req = new AuditRequest(
                eventId, UUID.randomUUID().toString(), "PASTE_CREATED", Instant.now());

        double storedBefore = registry.counter("audit.messages.consumed").count();
        double duplicatesBefore = registry.counter("consistency.audit.duplicate").count();

        assertEquals(200, controller.audit(req).getStatusCode().value());
        assertEquals(200, controller.audit(req).getStatusCode().value());

        // UNIQUE(event_id) guarantees at most one row; the counters prove
        // the first call stored it and the second was treated as a duplicate.
        assertTrue(repository.existsByEventId(eventId));
        assertEquals(storedBefore + 1, registry.counter("audit.messages.consumed").count());
        assertEquals(duplicatesBefore + 1, registry.counter("consistency.audit.duplicate").count());
    }
}
