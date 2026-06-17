package com.arenahub.infrastructure.persistence.payment;

import com.arenahub.domain.payment.vo.ChargeStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargeJpaRepository extends JpaRepository<ChargeJpaEntity, UUID> {

    Optional<ChargeJpaEntity> findByIdAndGroupId(UUID id, UUID groupId);

    boolean existsByReferenceMatchIdAndMemberIdAndStatusIn(
            UUID referenceMatchId, UUID memberId, List<ChargeStatus> statuses);

    @Query("""
            SELECT c FROM ChargeJpaEntity c
            WHERE c.groupId = :groupId
              AND (:status IS NULL OR c.status = :status)
              AND (:matchId IS NULL OR c.referenceMatchId = :matchId)
            ORDER BY c.createdAt DESC
            """)
    List<ChargeJpaEntity> findByGroupIdFiltered(
            @Param("groupId") UUID groupId,
            @Param("status") ChargeStatus status,
            @Param("matchId") UUID matchId,
            Pageable pageable);

    @Query("""
            SELECT COUNT(c) FROM ChargeJpaEntity c
            WHERE c.groupId = :groupId
              AND (:status IS NULL OR c.status = :status)
              AND (:matchId IS NULL OR c.referenceMatchId = :matchId)
            """)
    long countByGroupIdFiltered(
            @Param("groupId") UUID groupId,
            @Param("status") ChargeStatus status,
            @Param("matchId") UUID matchId);

    List<ChargeJpaEntity> findByReferenceMatchId(UUID referenceMatchId);

    List<ChargeJpaEntity> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
