package com.arenahub.infrastructure.persistence.user;

import com.arenahub.domain.user.PasswordResetToken;
import com.arenahub.domain.user.PasswordResetTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpa;

    public PasswordResetTokenPersistenceAdapter(PasswordResetTokenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(PasswordResetTokenJpaEntity::toDomain);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        return jpa.save(PasswordResetTokenJpaEntity.fromDomain(token)).toDomain();
    }

    @Override
    public void deleteAllExpired() {
        jpa.deleteAllByExpiresAtBefore(Instant.now());
    }
}
