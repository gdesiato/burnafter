package com.burnafter.burnafter.model;

import java.time.Instant;
import java.util.UUID;

public class Paste {
    public enum Kind { TEXT }

    private UUID id;
    private Kind kind;
    private String contentText;     // text content only (MVP)
    private Instant createdAt;
    private Instant expireAt;
    private int viewsLeft;
    private boolean burnAfterRead;
    private boolean hasPassword;
    private String passwordHash;    // simple SHA-256 base64 for MVP

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }

    public int getViewsLeft() {
        return viewsLeft;
    }

    public void setViewsLeft(int viewsLeft) {
        this.viewsLeft = viewsLeft;
    }

    public boolean isBurnAfterRead() {
        return burnAfterRead;
    }

    public void setBurnAfterRead(boolean burnAfterRead) {
        this.burnAfterRead = burnAfterRead;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
