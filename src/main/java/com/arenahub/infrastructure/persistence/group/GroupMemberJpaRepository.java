package com.arenahub.infrastructure.persistence.group;

import com.arenahub.domain.group.vo.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberJpaRepository extends JpaRepository<GroupMemberJpaEntity, UUID> {

    List<GroupMemberJpaEntity> findByGroupId(UUID groupId);

    Optional<GroupMemberJpaEntity> findByGroupIdAndUserId(UUID groupId, UUID userId);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    long countByGroupIdAndRole(UUID groupId, GroupRole role);
}
