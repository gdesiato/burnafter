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

@Component
@EnableScheduling
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxRepository repository;
    private final RestClient auditRestClient;

    public OutboxProcessor(OutboxRepository repository,
                           RestClient auditRestClient) {
        this.repository = repository;
        this.auditRestClient = auditRestClient;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {

        var events =
                repository.findTop10ByProcessedFalseOrderByCreatedAtAsc();

        if (events.isEmpty()) {
            return;
        }

        log.info("OutboxProcessor found {} pending events", events.size());

        for (var event : events) {

            try {
                log.info("Processing event {}", event.getId());

                auditRestClient.post()
                        .uri("/audit")
                        .body(new AuditRequest(
                                event.getAggregateId().toString(),
                                event.getEventType(),
                                System.currentTimeMillis()
                        ))
                        .retrieve()
                        .toBodilessEntity();

                event.markProcessed();

                log.info("Event {} processed successfully", event.getId());

            } catch (Exception e) {

                log.warn("Failed to process event {}. Will retry.",
                        event.getId());

            }
        }
    }
}

