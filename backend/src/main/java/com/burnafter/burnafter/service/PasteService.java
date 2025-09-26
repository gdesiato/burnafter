package com.burnafter.burnafter.service;

import com.burnafter.burnafter.dtos.*;
import com.burnafter.burnafter.model.Paste;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
        if (!"TEXT".equalsIgnoreCase(req.kind)) throw new IllegalArgumentException("Only TEXT supported");

        // TTL + views as you already do
        int views = Math.max(1, Math.min(10, req.views));
        Duration ttl = clampTtl(parseTtl(req.expiresIn));
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        String hash = null;
        boolean hasPwd = req.password != null && !req.password.isBlank();
        if (hasPwd) hash = sha256b64(req.password);

        Paste p;

        if (req.isEncrypted()) {
            // ZK mode: NEVER store plaintext
            // (Optionally enforce a max on ciphertext size, not plaintext.)
            p = new Paste(
                    id,
                    Paste.Kind.TEXT,
                    null,              // contentText
                    now,
                    now.plus(ttl),
                    views,
                    req.burnAfterRead,
                    false,             // hasPassword false in ZK link-key mode
                    null               // passwordHash
            );
            p.setEncrypted(true);
            p.setCiphertext(req.ciphertext);
            p.setIv(req.iv);

        } else if (req.isPlaintext()) {
            // Legacy plaintext mode (what you have today)
            var bytes = req.content.getBytes(StandardCharsets.UTF_8);
            if (bytes.length > maxTextBytes) throw new IllegalArgumentException("Content too large");

            p = new Paste(
                    id,
                    Paste.Kind.TEXT,
                    req.content,
                    now,
                    now.plus(ttl),
                    views,
                    req.burnAfterRead,
                    hasPwd,
                    hash
            );

        } else {
            throw new IllegalArgumentException("Provide either content or ciphertext+iv");
        }

        store.put(id, p);

        String readBase = (publicBaseUrl != null && !publicBaseUrl.isBlank()) ? publicBaseUrl : baseUrl;
        return new CreatePasteResponse(
                id.toString(),
                readBase + "/p/" + id,   // the client will append #key for ZK
                p.getExpireAt(),
                p.getViewsLeft()
        );
    }

    private Duration clampTtl(Duration ttl) {
        var maxTtl = Duration.ofMinutes(maxTtlMinutes);
        if (ttl.compareTo(maxTtl) > 0) ttl = maxTtl;
        if (ttl.isZero() || ttl.isNegative()) ttl = Duration.ofMinutes(defaultTtlMinutes);
        return ttl;
    }

    private static String sha256b64(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public DataResponse data(UUID id) {
        var p = store.get(id);
        if (p == null || expired(p)) { store.remove(id); return null; }
        if (!p.isEncrypted()) return null; // only for ZK records

        // Decrement views
        int remaining = Math.max(0, p.getViewsLeft() - 1);
        p.setViewsLeft(remaining);
        if (p.isBurnAfterRead() || remaining <= 0) store.remove(id);

        return new DataResponse(p.getIv(), p.getCiphertext());
    }

    private boolean expired(Paste p) {
        return Instant.now().isAfter(p.getExpireAt());
    }

    private Duration parseTtl(String s) {
        if (s == null || s.isBlank()) return Duration.ofMinutes(defaultTtlMinutes);
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

    public int purgeExpired() {
        int removed = 0;
        for (Iterator<Map.Entry<UUID, Paste>> it = store.entrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            if (expired(entry.getValue())) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    public MetaResponse meta(UUID id) {
        var p = store.get(id);
        if (p == null || expired(p)) { store.remove(id); return null; }

        // For encrypted pastes, protectedByPassword is always false (link-key mode).
        boolean protectedByPassword = !p.isEncrypted() && p.isHasPassword();

        return new MetaResponse(
                p.getKind().name(),
                p.getExpireAt(),
                p.getViewsLeft(),
                protectedByPassword
        );
    }

    /**
     * Legacy plaintext open: returns plaintext content and decrements views.
     * For encrypted (ZK) pastes, this returns null (controller will send 404).
     * Frontend must use GET /api/pastes/{id}/data for ZK.
     */
    public OpenResponse open(UUID id, OpenRequest req) {
        var p = store.get(id);
        if (p == null || expired(p)) { store.remove(id); return null; }

        // Encrypted pastes are not opened via this path
        if (p.isEncrypted()) return null;

        if (p.isHasPassword()) {
            var given = (req == null) ? null : req.password();
            if (given == null || given.isBlank()) throw new SecurityException("Password required");
            if (!Objects.equals(p.getPasswordHash(), sha256b64(given))) throw new SecurityException("Invalid password");
        }

        int remaining = Math.max(0, p.getViewsLeft() - 1);
        p.setViewsLeft(remaining);
        if (p.isBurnAfterRead() || remaining <= 0) store.remove(id);

        return new OpenResponse(p.getKind().name(), p.getContentText(), remaining);
    }
}
