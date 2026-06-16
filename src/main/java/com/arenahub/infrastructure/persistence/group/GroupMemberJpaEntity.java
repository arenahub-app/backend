package com.arenahub.infrastructure.persistence.group;

import com.arenahub.domain.group.GroupMember;
import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.group.vo.Skill;
import com.arenahub.domain.group.vo.SkillSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
public class GroupMemberJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GroupRole role;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_source", nullable = false, length = 10)
    private SkillSource skillSource;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PlayerPosition position;

    @Column(name = "is_subscriber", nullable = false)
    private boolean isSubscriber;

    @Column(name = "subscription_amount", precision = 10, scale = 2)
    private BigDecimal subscriptionAmount;

    @Column(name = "presence_banned", nullable = false)
    private boolean presenceBanned;

    @Column(name = "presence_ban_reason", columnDefinition = "TEXT")
    private String presenceBanReason;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public GroupMember toDomain() {
        return GroupMember.reconstitute(id, groupId, userId, role,
                new Skill(skill), skillSource, position,
                isSubscriber, subscriptionAmount,
                presenceBanned, presenceBanReason,
                joinedAt, updatedAt);
    }

    public static GroupMemberJpaEntity fromDomain(GroupMember member) {
        GroupMemberJpaEntity e = new GroupMemberJpaEntity();
        e.id = member.getId();
        e.groupId = member.getGroupId();
        e.userId = member.getUserId();
        e.role = member.getRole();
        e.skill = member.getSkill().value();
        e.skillSource = member.getSkillSource();
        e.position = member.getPosition();
        e.isSubscriber = member.isSubscriber();
        e.subscriptionAmount = member.getSubscriptionAmount();
        e.presenceBanned = member.isPresenceBanned();
        e.presenceBanReason = member.getPresenceBanReason();
        e.joinedAt = member.getJoinedAt();
        e.updatedAt = member.getUpdatedAt();
        return e;
    }
}
