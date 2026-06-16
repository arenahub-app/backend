package com.arenahub.domain.group;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class GroupInvite {

    private static final int MAX_USAGES = 50;
    private static final int EXPIRY_DAYS = 7;

    private UUID id;
    private UUID groupId;
    private UUID createdBy;
    private String token;
    private int usageCount;
    private int maxUsages;
    private boolean active;
    private Instant expiresAt;
    private Instant createdAt;

    private GroupInvite() {}

    public static GroupInvite create(UUID groupId, UUID createdBy) {
        GroupInvite invite = new GroupInvite();
        invite.id = UUID.randomUUID();
        invite.groupId = groupId;
        invite.createdBy = createdBy;
        invite.token = UUID.randomUUID().toString();
        invite.usageCount = 0;
        invite.maxUsages = MAX_USAGES;
        invite.active = true;
        invite.expiresAt = Instant.now().plus(EXPIRY_DAYS, ChronoUnit.DAYS);
        invite.createdAt = Instant.now();
        return invite;
    }

    public static GroupInvite reconstitute(UUID id, UUID groupId, UUID createdBy, String token,
                                           int usageCount, int maxUsages, boolean active,
                                           Instant expiresAt, Instant createdAt) {
        GroupInvite invite = new GroupInvite();
        invite.id = id;
        invite.groupId = groupId;
        invite.createdBy = createdBy;
        invite.token = token;
        invite.usageCount = usageCount;
        invite.maxUsages = maxUsages;
        invite.active = active;
        invite.expiresAt = expiresAt;
        invite.createdAt = createdAt;
        return invite;
    }

    public boolean isValid() {
        return active && usageCount < maxUsages && Instant.now().isBefore(expiresAt);
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() { return id; }
    public UUID getGroupId() { return groupId; }
    public UUID getCreatedBy() { return createdBy; }
    public String getToken() { return token; }
    public int getUsageCount() { return usageCount; }
    public int getMaxUsages() { return maxUsages; }
    public boolean isActive() { return active; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
