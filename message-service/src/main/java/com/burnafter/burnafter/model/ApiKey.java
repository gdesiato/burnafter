package com.burnafter.burnafter.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyHash;

    @Column(nullable = false, unique = true, length = 12)
    private String prefix;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Integer rateLimitPerHour;

    @Column(nullable = false)
    private String resilienceProfile;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ApiKey() {}

    public ApiKey(String keyHash,
                  String prefix,
                  Integer rateLimitPerHour,
                  String resilienceProfile) {

        this.keyHash = keyHash;
        this.prefix = prefix;
        this.rateLimitPerHour = rateLimitPerHour;
        this.resilienceProfile = resilienceProfile;
        this.enabled = true;
        this.createdAt = LocalDateTime.now();
    }

    public String getKeyHash() {
        return keyHash;
    }

    public String getPrefix() {
        return prefix;
    }

    public Integer getRateLimitPerHour() {
        return rateLimitPerHour;
    }

    public String getResilienceProfile() {
        return resilienceProfile;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        this.enabled = false;
    }
}
