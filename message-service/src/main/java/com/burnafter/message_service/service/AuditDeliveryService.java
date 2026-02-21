package com.burnafter.message_service.service;

import com.burnafter.message_service.dtos.AuditRequest;
import com.burnafter.message_service.outbox.OutboxEvent;
import com.burnafter.message_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Service
public class AuditDeliveryService {

    private final RestClient auditClient;
    private final OutboxRepository repository;

    public AuditDeliveryService(RestClient auditClient,
                                OutboxRepository repository) {
        this.auditClient = auditClient;
        this.repository = repository;
    }

    public void deliver(OutboxEvent event) {

        try {

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

            event.markProcessed();
            repository.save(event);

        } catch (Exception ex) {

            event.incrementRetryWithBackoff(ex);
            repository.save(event);
        }
    }
}
