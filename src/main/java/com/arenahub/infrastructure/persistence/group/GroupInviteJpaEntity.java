package com.arenahub.infrastructure.persistence.group;

import com.arenahub.domain.group.GroupInvite;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_invites")
@Getter
@Setter
@NoArgsConstructor
public class GroupInviteJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    private UUID createdBy;

    @Column(nullable = false, length = 36)
    private String token;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "max_usages", nullable = false)
    private int maxUsages;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public GroupInvite toDomain() {
        return GroupInvite.reconstitute(id, groupId, createdBy, token,
                usageCount, maxUsages, active, expiresAt, createdAt);
    }

    public static GroupInviteJpaEntity fromDomain(GroupInvite invite) {
        GroupInviteJpaEntity e = new GroupInviteJpaEntity();
        e.id = invite.getId();
        e.groupId = invite.getGroupId();
        e.createdBy = invite.getCreatedBy();
        e.token = invite.getToken();
        e.usageCount = invite.getUsageCount();
        e.maxUsages = invite.getMaxUsages();
        e.active = invite.isActive();
        e.expiresAt = invite.getExpiresAt();
        e.createdAt = invite.getCreatedAt();
        return e;
    }
}
