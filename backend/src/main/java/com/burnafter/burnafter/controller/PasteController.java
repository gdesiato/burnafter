package com.burnafter.burnafter.controller;

import com.burnafter.burnafter.dtos.*;
import com.burnafter.burnafter.service.PasteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.SecureRandom;

@RestController
@RequestMapping("/api/pastes")
public class PasteController {
    private final PasteService service;
    private static final SecureRandom RNG = new SecureRandom();

    public PasteController(PasteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreatePasteResponse> create(@Valid @RequestBody CreatePasteRequest req,
                                                      HttpServletRequest http) {
        var base = externalBaseUrl(http);
        var resp = service.create(req, base);
        return ResponseEntity.created(URI.create(resp.readUrl()))
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetaResponse> meta(@PathVariable UUID id) {
        var meta = service.meta(id);
        if (meta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .body(meta);
    }

    // Hardened: never leak if a paste exists or not
    // Accept String so a malformed UUID does NOT cause 400 (which would leak).
    @GetMapping("/{id}/data")
    public ResponseEntity<ReadPasteResponse> data(@PathVariable String id) {
        long start = System.nanoTime();

        ReadPasteResponse body;
        try {
            UUID uuid = UUID.fromString(id);
            var data = service.data(uuid); // null -> not found / expired / already read
            if (data != null) {
                body = new ReadPasteResponse(
                        true,
                        data.iv(),
                        data.ciphertext(),
                        null,  // keep the field but don’t reveal counts
                        randomPad(256, 768)
                );
            } else {
                body = new ReadPasteResponse(false, null, null, null, randomPad(256, 768));
            }
        } catch (Exception any) {
            // Malformed id or any other error -> same generic response
            body = new ReadPasteResponse(false, null, null, null, randomPad(256, 768));
        }

        // Add small randomized delay to reduce timing probes
        sleepUntilAtLeast(start, 120, 220);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<OpenResponse> open(@PathVariable UUID id,
                                             @RequestBody(required = false) OpenRequest req) {
        try {
            var out = service.open(id, req);
            if (out == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .header("Pragma", "no-cache")
                    .body(out);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private static String externalBaseUrl(HttpServletRequest req) {
        return ServletUriComponentsBuilder.fromRequest(req)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString();
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
