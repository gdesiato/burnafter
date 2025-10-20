package com.burnafter.burnafter.controller;

import com.burnafter.burnafter.dtos.*;
import com.burnafter.burnafter.service.PasteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/pastes")
public class PasteController {
    private final PasteService service;

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

    @GetMapping("/{id}/data")
    public ResponseEntity<DataResponse> data(@PathVariable UUID id) {
        var data = service.data(id);
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .body(data);
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
}

