package com.arenahub.infrastructure.persistence.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupInviteJpaRepository extends JpaRepository<GroupInviteJpaEntity, UUID> {

    List<GroupInviteJpaEntity> findByGroupIdAndActiveTrue(UUID groupId);

    Optional<GroupInviteJpaEntity> findByToken(String token);

    @Modifying
    @Query("UPDATE GroupInviteJpaEntity i SET i.active = false WHERE i.groupId = :groupId")
    void deactivateAllByGroupId(@Param("groupId") UUID groupId);
}
