package com.arenahub.infrastructure.persistence.user;

import com.arenahub.domain.user.PasswordResetToken;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetTokenJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PasswordResetToken toDomain() {
        return PasswordResetToken.reconstitute(id, userId, tokenHash, expiresAt, usedAt, createdAt);
    }

    public static PasswordResetTokenJpaEntity fromDomain(PasswordResetToken prt) {
        PasswordResetTokenJpaEntity e = new PasswordResetTokenJpaEntity();
        e.id = prt.getId();
        e.userId = prt.getUserId();
        e.tokenHash = prt.getTokenHash();
        e.expiresAt = prt.getExpiresAt();
        e.usedAt = prt.getUsedAt();
        e.createdAt = prt.getCreatedAt();
        return e;
    }
}
