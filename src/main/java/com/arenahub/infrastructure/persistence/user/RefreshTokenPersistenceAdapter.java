package com.arenahub.infrastructure.persistence.user;

import com.arenahub.domain.user.RefreshToken;
import com.arenahub.domain.user.RefreshTokenRepository;
import com.arenahub.domain.user.RefreshTokenStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpa;

    public RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return jpa.save(RefreshTokenJpaEntity.fromDomain(token)).toDomain();
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        jpa.updateStatusByUserId(userId, RefreshTokenStatus.REVOKED);
    }

    @Override
    public void deleteAllExpiredBefore(Instant cutoff) {
        jpa.deleteAllByExpiresAtBefore(cutoff);
    }
}
