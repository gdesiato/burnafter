package com.burnafter.burnafter.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pastes")
public class Paste {

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

    public enum Kind { TEXT }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    @Column(nullable = false, length = 100000)
    private String ciphertext;

    @Column(nullable = false)
    private String iv;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expireAt;

    private int viewsLeft;

    @Column(nullable = false)
    private boolean burnAfterRead;

    @Version
    private Long version;

    public Paste() {} // required by JPA

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
