package com.burnafter.burnafter.model;

import java.time.Instant;
import java.util.UUID;

public class Paste {

    public enum Kind { TEXT }

    private UUID id;
    private Kind kind;

    // PLAINTEXT path
    private String contentText;

    // ENCRYPTED path (E2EE)
    private boolean encrypted;
    private String ciphertext; // base64
    private String iv;         // base64 (12-byte GCM IV)

    private final Instant createdAt;
    private Instant expireAt;
    private int viewsLeft;
    private boolean burnAfterRead;

    // legacy plaintext password gating (optional)
    private boolean hasPassword;
    private String passwordHash;

    // ---- constructor you already used for plaintext ----
    public Paste(UUID id, Kind kind, String contentText, Instant createdAt, Instant expireAt,
                 int viewsLeft, boolean burnAfterRead, boolean hasPassword, String passwordHash) {
        this.id = id;
        this.kind = kind;
        this.contentText = contentText;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.viewsLeft = viewsLeft;
        this.burnAfterRead = burnAfterRead;
        this.hasPassword = hasPassword;
        this.passwordHash = passwordHash;
    }

    // ---- getters/setters ----
    public UUID getId() { return id; }
    public Kind getKind() { return kind; }

    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }

    public boolean isExpired() {
        return expireAt != null && Instant.now().isAfter(expireAt);
    }

    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }

    public String getCiphertext() { return ciphertext; }
    public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }

    public String getIv() { return iv; }
    public void setIv(String iv) { this.iv = iv; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpireAt() { return expireAt; }
    public void setExpireAt(Instant expireAt) { this.expireAt = expireAt; }

    public int getViewsLeft() { return viewsLeft; }
    public void setViewsLeft(int viewsLeft) { this.viewsLeft = viewsLeft; }

    public boolean isBurnAfterRead() { return burnAfterRead; }
    public void setBurnAfterRead(boolean burnAfterRead) { this.burnAfterRead = burnAfterRead; }

    public boolean isHasPassword() { return hasPassword; }
    public void setHasPassword(boolean hasPassword) { this.hasPassword = hasPassword; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
