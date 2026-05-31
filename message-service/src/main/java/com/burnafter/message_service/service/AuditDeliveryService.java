package com.burnafter.message_service.service;

import com.burnafter.message_service.dtos.AuditRequest;
import com.burnafter.message_service.metrics.MetricsService;
import com.burnafter.message_service.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Service
public class AuditDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(AuditDeliveryService.class);

    private final RestClient auditClient;
    private final MetricsService metricsService;

    public AuditDeliveryService(RestClient auditClient,
                                MetricsService metricsService) {
        this.auditClient = auditClient;
        this.metricsService = metricsService;
    }

    public void deliver(OutboxEvent event) {
        try {

            log.info(
                    "Sending audit event {} createdAt={}",
                    event.getId(),
                    event.getCreatedAt()
            );

            auditClient.post()
                    .uri("/audit")
                    .body(new AuditRequest(
                            event.getId().toString(),
                            event.getAggregateId().toString(),
                            event.getEventType(),
                            Instant.now(),
                            "message-service",
                            event.getCreatedAt()
                    ))
                    .retrieve()
                    .toBodilessEntity();

            // success
            metricsService.incrementOutboxProcessed();
        } catch (Exception e) {
            // failure
            metricsService.incrementOutboxFailures();
            throw e;
        }
    }
}
