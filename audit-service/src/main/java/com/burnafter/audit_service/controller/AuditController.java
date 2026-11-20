package com.burnafter.audit_service.controller;

import com.burnafter.audit_service.dto.AuditRequest;
import com.burnafter.audit_service.metrics.MetricsService;
import com.burnafter.audit_service.repository.AuditEventRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private static final Logger log =
            LoggerFactory.getLogger(AuditController.class);

    private final AuditEventRepository auditEventRepository;
    private final MetricsService metricsService;

    public AuditController(
            AuditEventRepository auditEventRepository,
            MetricsService metricsService) {
        this.auditEventRepository = auditEventRepository;
        this.metricsService = metricsService;
    }

    /**
     * Idempotent consumer. The message-service outbox delivers at-least-once,
     * so the same eventId can arrive more than once (crash after delivery,
     * stale PROCESSING reclaim). A duplicate is acknowledged with 200 so the
     * producer stops retrying, and is counted instead of stored twice.
     * A malformed request is rejected with 400 by @Valid before touching the DB.
     */
    @PostMapping
    public ResponseEntity<Void> audit(@Valid @RequestBody AuditRequest request) {
        Instant consumedAt = Instant.now();

        int inserted = auditEventRepository.insertIfAbsent(
                request.eventId(),
                request.aggregateId(),
                request.eventType(),
                consumedAt,
                request.outboxCreatedAt(),
                consumedAt
        );

        if (inserted == 0) {
            log.info("Duplicate audit event {} ignored", request.eventId());
            metricsService.incrementDuplicateAudit();
            return ResponseEntity.ok().build();
        }

        metricsService.incrementMessagesConsumed();

        // Only first deliveries feed the consistency-lag metric;
        // duplicates would skew it upwards.
        metricsService.recordDivergence(
                Duration.between(request.outboxCreatedAt(), consumedAt));

        log.debug("event={} outboxCreatedAt={}",
                request.eventId(), request.outboxCreatedAt());

        return ResponseEntity.ok().build();
    }
}
