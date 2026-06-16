package com.arenahub.infrastructure.persistence.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupJpaRepository extends JpaRepository<GroupJpaEntity, UUID> {

    Optional<GroupJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

    @Query(value = """
            SELECT g.id, g.name, g.sport, g.photo_url AS photoUrl, g.status,
                   COUNT(m.id) AS memberCount, my.role AS myRole
            FROM groups g
            JOIN group_members my ON my.group_id = g.id AND my.user_id = :userId
            JOIN group_members m ON m.group_id = g.id
            WHERE g.deleted_at IS NULL
            GROUP BY g.id, g.name, g.sport, g.photo_url, g.status, my.role
            ORDER BY g.name
            """, nativeQuery = true)
    List<GroupSummaryProjection> findGroupSummariesByUserId(@Param("userId") UUID userId);
}
