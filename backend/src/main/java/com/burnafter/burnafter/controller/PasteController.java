package com.burnafter.burnafter.controller;

import com.burnafter.burnafter.dtos.*;
import com.burnafter.burnafter.service.PasteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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
        var base = baseUrl(http);
        var resp = service.create(req, base);
        return ResponseEntity.created(URI.create(resp.readUrl())).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetaResponse> meta(@PathVariable UUID id) {
        var meta = service.meta(id);
        return meta == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(meta);
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<DataResponse> data(@PathVariable UUID id) {
        var data = service.data(id);
        return data == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(data);
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<OpenResponse> open(@PathVariable UUID id,
                                             @RequestBody(required = false) OpenRequest req) {
        try {
            var out = service.open(id, req);
            return out == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(out);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    private static String baseUrl(HttpServletRequest req) {
        var proto = req.getHeader("X-Forwarded-Proto");
        if (proto == null) proto = req.getScheme();
        var host = req.getHeader("X-Forwarded-Host");
        if (host == null) host = req.getHeader("Host");
        return proto + "://" + host;
    }
}
