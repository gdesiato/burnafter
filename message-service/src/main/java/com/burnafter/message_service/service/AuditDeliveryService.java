package com.burnafter.message_service.service;

import com.burnafter.message_service.dtos.AuditRequest;
import com.burnafter.message_service.outbox.OutboxEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Sends one outbox event to the audit service. Throws on any failure (timeout,
 * connection refused, non-2xx); retry, circuit breaker and metrics are handled
 * by the caller, OutboxProcessor.
 */
@Service
public class AuditDeliveryService {

    private final RestClient auditClient;

    public AuditDeliveryService(RestClient auditClient) {
        this.auditClient = auditClient;
    }

    public void deliver(OutboxEvent event) {
        auditClient.post()
                .uri("/audit")
                .body(new AuditRequest(
                        event.getId().toString(),
                        event.getAggregateId().toString(),
                        event.getEventType(),
                        event.getCreatedAt()))
                .retrieve()
                .toBodilessEntity();
    }
}
