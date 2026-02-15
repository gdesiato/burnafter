package com.burnafter.burnafter.outbox;

import com.burnafter.burnafter.dtos.AuditRequest;
import com.burnafter.burnafter.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxProcessor {

    private final OutboxRepository repository;
    private final RestClient auditClient;
    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    public OutboxProcessor(OutboxRepository repository,
                           RestClient auditClient) {
        this.repository = repository;
        this.auditClient = auditClient;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {

        List<OutboxEvent> events =
                repository.findTop20ByProcessedFalseAndNextAttemptAtBeforeOrderByCreatedAtAsc(
                        Instant.now());

        for (OutboxEvent event : events) {
            try {

                auditClient.post()
                        .uri("/audit")
                        .body(new AuditRequest(
                                event.getAggregateId().toString(),
                                event.getEventType(),
                                System.currentTimeMillis()
                        ))
                        .retrieve()
                        .toBodilessEntity();

                event.markProcessed();

                log.info("Outbox event {} delivered successfully", event.getId());

            } catch (Exception ex) {

                event.incrementRetryWithBackoff();

                log.warn("Outbox event {} failed. Retry #{}. Next attempt at {}",
                        event.getId(),
                        event.getRetryCount(),
                        event.getNextAttemptAt());

            }
        }
    }
}


