package com.burnafter.message_service.repository;

import com.burnafter.message_service.outbox.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop10ByProcessedFalseOrderByCreatedAtAsc();

    @Query("""
        SELECT e FROM OutboxEvent e
        WHERE e.status = :status
        AND e.nextAttemptAt <= :now
        ORDER BY e.createdAt ASC
        """)
    List<OutboxEvent> findReadyToProcess(
            @Param("status") OutboxEvent.Status status,
            @Param("now") Instant now
    );

}
