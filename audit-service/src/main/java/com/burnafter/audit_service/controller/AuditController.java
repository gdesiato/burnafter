package com.burnafter.audit_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @PostMapping
    public ResponseEntity<Void> audit(@RequestBody AuditRequest request) {

        System.out.println("Received audit event for messageId: " + request.messageId());

        return ResponseEntity.ok().build();
    }

    public record AuditRequest(
            String messageId,
            String action,
            long timestamp
    ) {}
}
