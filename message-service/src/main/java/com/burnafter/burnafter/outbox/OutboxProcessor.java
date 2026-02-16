package com.burnafter.burnafter.outbox;

import com.burnafter.burnafter.dtos.AuditRequest;
import com.burnafter.burnafter.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxRepository repository;
    private final RestClient auditClient;

    @Value("${outbox.batch-size:50}")
    private int batchSize;

    public OutboxProcessor(OutboxRepository repository,
                           RestClient auditClient) {
        this.repository = repository;
        this.auditClient = auditClient;
    }

    @Scheduled(fixedDelayString = "${outbox.processing-delay-ms:5000}")
    public void process() {

        List<OutboxEvent> events =
                repository.findReadyToProcess(Instant.now())
                        .stream()
                        .limit(batchSize)
                        .toList();

        if (events.isEmpty()) {
            return;
        }

        log.debug("Processing {} outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                processSingleEvent(event);
            } catch (Exception ex) {
                event.recordFailure(ex.getMessage());
                repository.save(event);
                log.error("Unexpected failure processing event {}",
                        event.getId(), ex);
            }
        }
    }

    @Transactional
    protected void processSingleEvent(OutboxEvent event) {

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
            repository.save(event);

            log.info("Outbox event {} delivered successfully",
                    event.getId());

        } catch (Exception ex) {

            event.incrementRetryWithBackoff();
            repository.save(event);

            if (event.getStatus() == OutboxEvent.Status.DEAD) {
                log.error("Outbox event {} moved to DEAD after {} retries",
                        event.getId(),
                        event.getRetryCount());
            } else {
                log.warn("Outbox event {} failed. Retry #{}. Next attempt at {}",
                        event.getId(),
                        event.getRetryCount(),
                        event.getNextAttemptAt());
            }
        }
    }
}


