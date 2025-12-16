package com.burnafter.burnafter.model;

import java.time.Instant;
import java.util.UUID;

public class Paste {

    public enum Kind { TEXT }

    private final UUID id;
    private final Kind kind;

    // Zero-knowledge encrypted payload (server never sees plaintext)
    // Base64-encoded AES-GCM ciphertext and 12-byte IV
    private final String ciphertext;
    private final String iv;

    private final Instant createdAt;
    private final Instant expireAt;

    private int viewsLeft;
    private final boolean burnAfterRead;

    public Paste(
            UUID id,
            Kind kind,
            String ciphertext,
            String iv,
            Instant createdAt,
            Instant expireAt,
            int viewsLeft,
            boolean burnAfterRead
    ) {
        this.id = id;
        this.kind = kind;
        this.ciphertext = ciphertext;
        this.iv = iv;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.viewsLeft = viewsLeft;
        this.burnAfterRead = burnAfterRead;
    }

    public UUID getId() {
        return id;
    }

    public Kind getKind() {
        return kind;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public String getIv() {
        return iv;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public int getViewsLeft() {
        return viewsLeft;
    }

    public boolean isBurnAfterRead() {
        return burnAfterRead;
    }

    // --- Domain logic ---

    public boolean isExpired() {
        return expireAt != null && Instant.now().isAfter(expireAt);
    }

    /**
     * Consume one view.
     * Caller is responsible for persisting the change.
     */
    public void consumeView() {
        if (viewsLeft > 0) {
            viewsLeft--;
        }
    }

    public boolean isDepleted() {
        return viewsLeft <= 0;
    }
}

