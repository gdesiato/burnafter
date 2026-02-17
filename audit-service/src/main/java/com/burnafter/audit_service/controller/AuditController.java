package com.burnafter.audit_service.controller;

import com.burnafter.audit_service.dto.AuditRequest;
import com.burnafter.audit_service.model.AuditEvent;
import com.burnafter.audit_service.repository.AuditEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    public AuditController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @PostMapping
    public ResponseEntity<Void> audit(@RequestBody AuditRequest request) {

        try {
            auditEventRepository.save(new AuditEvent(
                    request.eventId(),
                    request.aggregateId(),
                    request.eventType()
            ));
        } catch (DataIntegrityViolationException ex) {
            // Duplicate eventId → ignore (idempotency)
        }

        return ResponseEntity.ok().build();
    }
}
