package com.arenahub.infrastructure.persistence.user;

import com.arenahub.domain.user.RefreshToken;
import com.arenahub.domain.user.RefreshTokenStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private RefreshTokenStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RefreshToken toDomain() {
        return RefreshToken.reconstitute(id, userId, tokenHash, status, expiresAt, createdAt);
    }

    public static RefreshTokenJpaEntity fromDomain(RefreshToken rt) {
        RefreshTokenJpaEntity e = new RefreshTokenJpaEntity();
        e.id = rt.getId();
        e.userId = rt.getUserId();
        e.tokenHash = rt.getTokenHash();
        e.status = rt.getStatus();
        e.expiresAt = rt.getExpiresAt();
        e.createdAt = rt.getCreatedAt();
        return e;
    }
}
