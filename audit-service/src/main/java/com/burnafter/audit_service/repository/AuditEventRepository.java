package com.burnafter.audit_service.repository;

import com.burnafter.audit_service.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    boolean existsByEventId(String eventId);
}

