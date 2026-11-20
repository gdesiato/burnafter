package com.burnafter.message_service.controller;

import com.burnafter.message_service.dtos.*;
import com.burnafter.message_service.service.PasteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.SecureRandom;

@RestController
@RequestMapping("/api/pastes")
public class PasteController {
    private static final Logger log = LoggerFactory.getLogger(PasteController.class);

    private final PasteService service;
    private static final SecureRandom RNG = new SecureRandom();

    public PasteController(PasteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreatePasteResponse> create(@Valid @RequestBody CreatePasteRequest req) {
        log.info("Create paste request received");

        // No read link in the response: the link contains the key, which only
        // the browser knows, so the browser builds it.
        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .body(service.create(req));
    }

    // Hardened: never leak if a paste exists or not
    // Accept String so a malformed UUID does NOT cause 400 (which would leak).
    @GetMapping("/{id}/data")
    public ResponseEntity<ReadPasteResponse> data(@PathVariable String id) {
        long start = System.nanoTime();

        ReadPasteResponse body;

        try {
            UUID uuid = UUID.fromString(id);
            body = service.data(uuid)
                    .map(d -> new ReadPasteResponse(true, d.iv(), d.ciphertext(), randomPad(256, 768)))
                    .orElseGet(PasteController::emptyReadResponse);

        } catch (Exception ignored) {
            // Malformed UUID or any other error -> indistinguishable response
            body = emptyReadResponse();
        }

        // Add small randomized delay to reduce timing probes
        sleepUntilAtLeast(start, 120, 220);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }

    private static ReadPasteResponse emptyReadResponse() {
        return new ReadPasteResponse(
                false,
                null,
                null,
                randomPad(256, 768)
        );
    }

    // Side-channel mitigations
    private static void sleepUntilAtLeast(long startNanos, int minMs, int maxMs) {
        int targetMs = minMs + RNG.nextInt(Math.max(1, maxMs - minMs));
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        long remaining = targetMs - elapsedMs;
        if (remaining > 0) {
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String randomPad(int min, int max) {
        int len = min + RNG.nextInt(Math.max(1, max - min));
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(alphabet.charAt(RNG.nextInt(alphabet.length())));
        return sb.toString();
    }
}
