package com.burnafter.message_service.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pastes")
public class Paste {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    // Optimistic locking: an UPDATE/DELETE only succeeds if the row still has the
    // version we read, so two concurrent readers can't both use the last view.
    @Version
    private Long version;

    // --- Constructors ---

    protected Paste() {
        // required by JPA
    }

    public Paste(String ciphertext, String iv, Instant createdAt, Instant expireAt, int viewsLeft) {
        this.ciphertext = ciphertext;
        this.iv = iv;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
        this.viewsLeft = viewsLeft;
    }

    // --- Getters ---

    public UUID getId() { return id; }
    public String getCiphertext() { return ciphertext; }
    public String getIv() { return iv; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpireAt() { return expireAt; }
    public int getViewsLeft() { return viewsLeft; }

    // --- Domain Logic ---

    public boolean isExpired() {
        return Instant.now().isAfter(expireAt);
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
