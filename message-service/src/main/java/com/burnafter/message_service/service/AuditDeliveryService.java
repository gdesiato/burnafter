package com.burnafter.message_service.service;

import com.burnafter.message_service.dtos.AuditRequest;
import com.burnafter.message_service.outbox.OutboxEvent;
import com.burnafter.message_service.outbox.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

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
                        Instant.now(),
                        "message-service"
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
