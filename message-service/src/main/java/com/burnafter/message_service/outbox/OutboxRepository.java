package com.burnafter.message_service.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE (
                status = 'PENDING'
                AND next_attempt_at <= :now
            )
            OR (
                status = 'PROCESSING'
                AND processing_started_at <= :reclaimBefore
            )
        ORDER BY
            COALESCE(next_attempt_at, processing_started_at)
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true)
    List<OutboxEvent> claimBatch(
            @Param("now") Instant now,
            @Param("reclaimBefore") Instant reclaimBefore,
            @Param("limit") int limit
    );
}
