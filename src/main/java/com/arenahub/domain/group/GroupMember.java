package com.arenahub.domain.group;

import com.arenahub.domain.group.vo.GroupRole;
import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.group.vo.Skill;
import com.arenahub.domain.group.vo.SkillSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class GroupMember {

    private UUID id;
    private UUID groupId;
    private UUID userId;
    private GroupRole role;
    private Skill skill;
    private SkillSource skillSource;
    private PlayerPosition position;
    private boolean isSubscriber;
    private BigDecimal subscriptionAmount;
    private boolean presenceBanned;
    private String presenceBanReason;
    private Instant joinedAt;
    private Instant updatedAt;

    private GroupMember() {}

    public static GroupMember create(UUID groupId, UUID userId, GroupRole role) {
        GroupMember m = new GroupMember();
        m.id = UUID.randomUUID();
        m.groupId = groupId;
        m.userId = userId;
        m.role = role;
        m.skill = Skill.DEFAULT;
        m.skillSource = SkillSource.DEFAULT;
        m.isSubscriber = false;
        m.presenceBanned = false;
        m.joinedAt = Instant.now();
        m.updatedAt = Instant.now();
        return m;
    }

    public static GroupMember reconstitute(UUID id, UUID groupId, UUID userId, GroupRole role,
                                           Skill skill, SkillSource skillSource,
                                           PlayerPosition position, boolean isSubscriber,
                                           BigDecimal subscriptionAmount, boolean presenceBanned,
                                           String presenceBanReason, Instant joinedAt,
                                           Instant updatedAt) {
        GroupMember m = new GroupMember();
        m.id = id;
        m.groupId = groupId;
        m.userId = userId;
        m.role = role;
        m.skill = skill;
        m.skillSource = skillSource;
        m.position = position;
        m.isSubscriber = isSubscriber;
        m.subscriptionAmount = subscriptionAmount;
        m.presenceBanned = presenceBanned;
        m.presenceBanReason = presenceBanReason;
        m.joinedAt = joinedAt;
        m.updatedAt = updatedAt;
        return m;
    }

    public void changeRole(GroupRole newRole) {
        this.role = newRole;
        this.updatedAt = Instant.now();
    }

    public void updateSkill(Skill newSkill) {
        this.skill = newSkill;
        this.skillSource = SkillSource.MANUAL;
        this.updatedAt = Instant.now();
    }

    public void updatePosition(PlayerPosition newPosition) {
        this.position = newPosition;
        this.updatedAt = Instant.now();
    }

    public void updateSubscription(boolean isSubscriber, BigDecimal amount) {
        this.isSubscriber = isSubscriber;
        this.subscriptionAmount = amount;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getGroupId() { return groupId; }
    public UUID getUserId() { return userId; }
    public GroupRole getRole() { return role; }
    public Skill getSkill() { return skill; }
    public SkillSource getSkillSource() { return skillSource; }
    public PlayerPosition getPosition() { return position; }
    public boolean isSubscriber() { return isSubscriber; }
    public BigDecimal getSubscriptionAmount() { return subscriptionAmount; }
    public boolean isPresenceBanned() { return presenceBanned; }
    public String getPresenceBanReason() { return presenceBanReason; }
    public Instant getJoinedAt() { return joinedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
