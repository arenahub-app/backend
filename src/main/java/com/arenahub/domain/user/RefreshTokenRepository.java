package com.arenahub.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken token);

    void revokeAllByUserId(UUID userId);

    void deleteAllExpiredBefore(java.time.Instant cutoff);
}
