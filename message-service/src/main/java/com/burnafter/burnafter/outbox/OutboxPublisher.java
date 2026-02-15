package com.burnafter.burnafter.outbox;

import com.burnafter.burnafter.dtos.AuditRequest;
import com.burnafter.burnafter.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@EnableScheduling
public class OutboxPublisher {

    private final OutboxRepository repository;
    private final RestClient auditRestClient;

    public OutboxPublisher(OutboxRepository repository, RestClient auditRestClient) {
        this.repository = repository;
        this.auditRestClient = auditRestClient;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publish() {

        List<OutboxEvent> events =
                repository.findTop10ByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
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

            } catch (Exception e) {
                // do nothing, will retry later
            }
        }
    }
}

