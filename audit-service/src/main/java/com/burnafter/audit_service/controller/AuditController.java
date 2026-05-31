package com.burnafter.audit_service.controller;

import com.burnafter.audit_service.dto.AuditRequest;
import com.burnafter.audit_service.metrics.MetricsService;
import com.burnafter.audit_service.model.AuditEvent;
import com.burnafter.audit_service.repository.AuditEventRepository;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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

    @PostMapping
    public ResponseEntity<Void> audit(@RequestBody AuditRequest request) {
        try {
            Instant consumedAt = Instant.now();

            auditEventRepository.save(new AuditEvent(
                    request.eventId(),
                    request.aggregateId(),
                    request.eventType(),
                    request.outboxCreatedAt(),
                    consumedAt
            ));

            metricsService.incrementMessagesConsumed();

            long divergenceMs = Duration.between(
                    request.outboxCreatedAt(),
                    consumedAt
            ).toMillis();

            log.info(
                    "event={} outboxCreatedAt={}",
                    request.eventId(),
                    request.outboxCreatedAt()
            );

            metricsService.recordDivergence(divergenceMs);

        } catch (DataIntegrityViolationException ex) {
            log.error(
                    "Audit insert failed for event {}",
                    request.eventId(),
                    ex
            );
            throw ex;
        }
        return ResponseEntity.ok().build();
    }
}
