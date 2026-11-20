package com.burnafter.audit_service.repository;

import com.burnafter.audit_service.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    boolean existsByEventId(String eventId);

    /**
     * Idempotent insert: the UNIQUE(event_id) constraint decides atomically
     * whether this event is new. Returns 1 if inserted, 0 if it already existed.
     * Safe under concurrent duplicate deliveries (no check-then-insert race).
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO audit_events
            (event_id, aggregate_id, event_type, outbox_created_at, consumed_at)
        VALUES
            (:eventId, :aggregateId, :eventType, :outboxCreatedAt, :consumedAt)
        ON CONFLICT (event_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") String eventId,
                       @Param("aggregateId") String aggregateId,
                       @Param("eventType") String eventType,
                       @Param("outboxCreatedAt") Instant outboxCreatedAt,
                       @Param("consumedAt") Instant consumedAt);
}
