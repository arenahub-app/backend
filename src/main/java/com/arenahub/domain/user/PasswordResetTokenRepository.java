package com.arenahub.domain.user;

import java.util.Optional;

public interface PasswordResetTokenRepository {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    PasswordResetToken save(PasswordResetToken token);

    void deleteAllExpired();
}
