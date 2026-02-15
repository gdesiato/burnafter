package com.burnafter.burnafter.service;

import com.burnafter.burnafter.dtos.*;
import com.burnafter.burnafter.exception.InvalidPasteException;
import com.burnafter.burnafter.exception.InvalidPasteReason;
import com.burnafter.burnafter.exception.PasteNotFoundException;
import com.burnafter.burnafter.outbox.OutboxEvent;
import com.burnafter.burnafter.model.Paste;
import com.burnafter.burnafter.repository.OutboxRepository;
import com.burnafter.burnafter.repository.PasteRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.*;
import java.util.*;

@Service
public class PasteService {

    private final PasteRepository pasteRepository;
    private final OutboxRepository outboxRepository;

    @Value("${app.maxTextBytes:20000}")     int maxTextBytes;
    @Value("${app.defaultTtlMinutes:1440}") int defaultTtlMinutes;
    @Value("${app.maxTtlMinutes:10080}")    int maxTtlMinutes;
    @Value("${app.publicBaseUrl:}")         private String publicBaseUrl;

    public PasteService(PasteRepository repository, OutboxRepository outboxRepository) {
        this.pasteRepository = repository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public CreatePasteResponse create(CreatePasteRequest req, String baseUrl) {

        if (!"TEXT".equalsIgnoreCase(req.kind))
            throw new InvalidPasteException(InvalidPasteReason.ONLY_TEXT_SUPPORTED);

        int views = Math.max(1, Math.min(10, req.views));
        Duration ttl = clampTtl(parseTtl(req.expiresIn));
        Instant now = Instant.now();

        validateCrypto(req);

        Paste p = new Paste(
                Paste.Kind.TEXT,
                req.ciphertext,
                req.iv,
                now,
                now.plus(ttl),
                views,
                req.burnAfterRead
        );

        pasteRepository.save(p);

        // 🔥 Only Outbox — no REST call
        OutboxEvent event = new OutboxEvent(
                p.getId(),
                "PASTE_CREATED",
                p.getId().toString()
        );

        outboxRepository.save(event);

        return buildResponse(p, baseUrl);
    }

    @Transactional
    public DataResponse data(UUID id) {

        Paste p = pasteRepository.findById(id)
                .orElseThrow(PasteNotFoundException::new);

        if (p.isExpired()) {
            pasteRepository.delete(p);
            throw new PasteNotFoundException();
        }

        p.consumeView();

        if (p.isBurnAfterRead() || p.isDepleted()) {
            pasteRepository.delete(p);
        }

        return new DataResponse(p.getIv(), p.getCiphertext());
    }

    @Transactional(readOnly = true)
    public MetaResponse meta(UUID id) {

        Paste p = pasteRepository.findById(id)
                .orElseThrow(PasteNotFoundException::new);

        if (p.isExpired())
            throw new PasteNotFoundException();

        return new MetaResponse(
                p.getKind().name(),
                p.getExpireAt(),
                p.getViewsLeft()
        );
    }

    @Transactional
    public int purgeExpired() {

        List<Paste> expired = pasteRepository.findByExpireAtBefore(Instant.now());
        pasteRepository.deleteAll(expired);
        return expired.size();
    }

    private void validateCrypto(CreatePasteRequest req) {

        if (!isB64(req.ciphertext) || !isB64(req.iv))
            throw new InvalidPasteException(InvalidPasteReason.INVALID_BASE64);

        byte[] ivBytes = Base64.getDecoder().decode(req.iv);
        if (ivBytes.length != 12)
            throw new InvalidPasteException(InvalidPasteReason.INVALID_IV_LENGTH);

        if (Base64.getDecoder().decode(req.ciphertext).length > maxTextBytes * 4)
            throw new InvalidPasteException(InvalidPasteReason.CIPHERTEXT_TOO_LARGE);
    }

    private boolean isB64(String s) {
        try {
            Base64.getDecoder().decode(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Duration clampTtl(Duration ttl) {
        Duration maxTtl = Duration.ofMinutes(maxTtlMinutes);
        if (ttl.compareTo(maxTtl) > 0) ttl = maxTtl;
        if (ttl.isZero() || ttl.isNegative())
            ttl = Duration.ofMinutes(defaultTtlMinutes);
        return ttl;
    }

    private Duration parseTtl(String s) {
        if (s == null || s.isBlank())
            return Duration.ofMinutes(defaultTtlMinutes);

        s = s.trim().toLowerCase(Locale.ROOT);
        try {
            if (s.endsWith("min")) return Duration.ofMinutes(Long.parseLong(s.replace("min","")));
            if (s.endsWith("h"))   return Duration.ofHours(Long.parseLong(s.replace("h","")));
            if (s.endsWith("d"))   return Duration.ofDays(Long.parseLong(s.replace("d","")));
            return Duration.ofMinutes(Long.parseLong(s));
        } catch (Exception e) {
            return Duration.ofMinutes(defaultTtlMinutes);
        }
    }

    private CreatePasteResponse buildResponse(Paste p, String baseUrl) {

        String readBase = (publicBaseUrl != null && !publicBaseUrl.isBlank())
                ? publicBaseUrl
                : baseUrl;

        return new CreatePasteResponse(
                p.getId().toString(),
                readBase + "/p/" + p.getId(),
                p.getExpireAt(),
                p.getViewsLeft()
        );
    }
}
