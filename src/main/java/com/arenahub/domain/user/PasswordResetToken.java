package com.arenahub.domain.user;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class PasswordResetToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant createdAt;

    private PasswordResetToken() {}

    public static PasswordResetToken issue(UUID userId, String tokenHash, Duration ttl) {
        PasswordResetToken prt = new PasswordResetToken();
        prt.id = UUID.randomUUID();
        prt.userId = userId;
        prt.tokenHash = tokenHash;
        prt.expiresAt = Instant.now().plus(ttl);
        prt.createdAt = Instant.now();
        return prt;
    }

    public static PasswordResetToken reconstitute(UUID id, UUID userId, String tokenHash,
                                                   Instant expiresAt, Instant usedAt,
                                                   Instant createdAt) {
        PasswordResetToken prt = new PasswordResetToken();
        prt.id = id;
        prt.userId = userId;
        prt.tokenHash = tokenHash;
        prt.expiresAt = expiresAt;
        prt.usedAt = usedAt;
        prt.createdAt = createdAt;
        return prt;
    }

    public boolean isValid() {
        return usedAt == null && Instant.now().isBefore(expiresAt);
    }

    public void markAsUsed() {
        this.usedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
