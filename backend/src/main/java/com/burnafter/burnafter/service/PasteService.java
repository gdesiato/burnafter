package com.burnafter.burnafter.service;

import com.burnafter.burnafter.dtos.*;
import com.burnafter.burnafter.exception.InvalidPasteException;
import com.burnafter.burnafter.exception.InvalidPasteReason;
import com.burnafter.burnafter.exception.PasteNotFoundException;
import com.burnafter.burnafter.model.Paste;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasteService {

    private final Map<UUID, Paste> store = new ConcurrentHashMap<>();

    @Value("${app.maxTextBytes:20000}")     int maxTextBytes;
    @Value("${app.defaultTtlMinutes:1440}") int defaultTtlMinutes;
    @Value("${app.maxTtlMinutes:10080}")    int maxTtlMinutes;
    @Value("${app.publicBaseUrl:}")         private String publicBaseUrl;

    public CreatePasteResponse create(CreatePasteRequest req, String baseUrl) {
        if (!"TEXT".equalsIgnoreCase(req.kind))
            throw new InvalidPasteException(InvalidPasteReason.ONLY_TEXT_SUPPORTED);

        int views = Math.max(1, Math.min(10, req.views));
        Duration ttl = clampTtl(parseTtl(req.expiresIn));
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        // Strict ZK validation
        if (!isB64(req.ciphertext) || !isB64(req.iv))
            throw new InvalidPasteException(InvalidPasteReason.INVALID_BASE64);

        byte[] ivBytes = Base64.getDecoder().decode(req.iv);
        if (ivBytes.length != 12)
            throw new InvalidPasteException(InvalidPasteReason.INVALID_IV_LENGTH);

        if (Base64.getDecoder().decode(req.ciphertext).length > maxTextBytes * 4)
            throw new InvalidPasteException(InvalidPasteReason.CIPHERTEXT_TOO_LARGE);

        Paste p = new Paste(
                id,
                Paste.Kind.TEXT,
                req.ciphertext,
                req.iv,
                now,
                now.plus(ttl),
                views,
                req.burnAfterRead
        );

        store.put(id, p);

        String readBase = (publicBaseUrl != null && !publicBaseUrl.isBlank())
                ? publicBaseUrl
                : baseUrl;

        return new CreatePasteResponse(
                id.toString(),
                readBase + "/p/" + id,
                p.getExpireAt(),
                p.getViewsLeft()
        );
    }

    // Read-once encrypted data
    public DataResponse data(UUID id) {
        Paste p = store.get(id);
        if (p == null || expired(p)) {
            store.remove(id);
            throw new PasteNotFoundException();
        }
        p.consumeView();

        if (p.isBurnAfterRead() || p.isDepleted()) {
            store.remove(id);
        }
        return new DataResponse(p.getIv(), p.getCiphertext());
    }

    // Metadata
    public MetaResponse meta(UUID id) {
        Paste p = store.get(id);
        if (p == null || expired(p)) {
            store.remove(id);
            return null;
        }

        // ZK invariant: never password-protected
        return new MetaResponse(
                p.getKind().name(),
                p.getExpireAt(),
                p.getViewsLeft()
        );
    }

    public int purgeExpired() {
        int removed = 0;
        for (Iterator<Map.Entry<UUID, Paste>> it = store.entrySet().iterator(); it.hasNext();) {
            if (expired(it.next().getValue())) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    private boolean expired(Paste p) {
        return Instant.now().isAfter(p.getExpireAt());
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

    private static boolean isB64(String s) {
        try {
            Base64.getDecoder().decode(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
