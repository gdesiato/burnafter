package com.burnafter.message_service.outbox;

import com.burnafter.message_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxClaimService {

    private final OutboxRepository repository;

    public OutboxClaimService(OutboxRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<OutboxEvent> claimBatch(int batchSize) {

        List<OutboxEvent> events =
                repository.claimBatch(Instant.now(), batchSize);

        for (OutboxEvent e : events)
            e.markProcessing();

        return events;
    }
}
