package com.burnafter.message_service.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pastes")
public class Paste {

    public enum Kind { TEXT }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Kind kind;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ciphertext;

    @Column(nullable = false, length = 255)
    private String iv;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expire_at", nullable = false)
    private Instant expireAt;

    @Column(name = "views_left")
    private int viewsLeft;

    @Column(name = "burn_after_read", nullable = false)
    private boolean burnAfterRead;

    @Version
    private Long version;

    // --- Constructors ---

    protected Paste() {
        // required by JPA
    }

    public Paste(
            Kind kind,
            String ciphertext,
            String iv,
            Instant createdAt,
            Instant expireAt,
            int viewsLeft,
            boolean burnAfterRead
    ) {
        this.kind = kind;
        this.ciphertext = ciphertext;
        this.iv = iv;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.viewsLeft = viewsLeft;
        this.burnAfterRead = burnAfterRead;
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public Kind getKind() { return kind; }
    public String getCiphertext() { return ciphertext; }
    public String getIv() { return iv; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpireAt() { return expireAt; }
    public int getViewsLeft() { return viewsLeft; }
    public boolean isBurnAfterRead() { return burnAfterRead; }

    // --- Domain Logic ---

    public boolean isExpired() {
        return expireAt != null && Instant.now().isAfter(expireAt);
    }

    public void consumeView() {
        if (viewsLeft > 0) {
            viewsLeft--;
        }
    }

    public boolean isDepleted() {
        return viewsLeft <= 0;
    }
}
