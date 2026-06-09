package com.arenahub.infrastructure.persistence.user;

import com.arenahub.domain.user.RefreshTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity rt SET rt.status = :status WHERE rt.userId = :userId AND rt.status = 'ACTIVE'")
    void updateStatusByUserId(UUID userId, RefreshTokenStatus status);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity rt WHERE rt.expiresAt < :cutoff")
    void deleteAllByExpiresAtBefore(Instant cutoff);
}
