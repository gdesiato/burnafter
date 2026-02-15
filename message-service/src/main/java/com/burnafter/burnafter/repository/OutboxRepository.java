package com.burnafter.burnafter.repository;

import com.burnafter.burnafter.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop10ByProcessedFalseOrderByCreatedAtAsc();
    List<OutboxEvent> findTop20ByProcessedFalseAndNextAttemptAtBeforeOrderByCreatedAtAsc(Instant now);
}
