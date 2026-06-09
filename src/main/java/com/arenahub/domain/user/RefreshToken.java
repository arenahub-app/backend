package com.arenahub.domain.user;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private RefreshTokenStatus status;
    private Instant expiresAt;
    private Instant createdAt;

    private RefreshToken() {}

    public static RefreshToken issue(UUID userId, String tokenHash, Duration ttl) {
        RefreshToken rt = new RefreshToken();
        rt.id = UUID.randomUUID();
        rt.userId = userId;
        rt.tokenHash = tokenHash;
        rt.status = RefreshTokenStatus.ACTIVE;
        rt.expiresAt = Instant.now().plus(ttl);
        rt.createdAt = Instant.now();
        return rt;
    }

    public static RefreshToken reconstitute(UUID id, UUID userId, String tokenHash,
                                            RefreshTokenStatus status, Instant expiresAt,
                                            Instant createdAt) {
        RefreshToken rt = new RefreshToken();
        rt.id = id;
        rt.userId = userId;
        rt.tokenHash = tokenHash;
        rt.status = status;
        rt.expiresAt = expiresAt;
        rt.createdAt = createdAt;
        return rt;
    }

    public boolean isValid() {
        return status == RefreshTokenStatus.ACTIVE && Instant.now().isBefore(expiresAt);
    }

    public void revoke() {
        this.status = RefreshTokenStatus.REVOKED;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public RefreshTokenStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
