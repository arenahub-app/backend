package com.arenahub.domain.group;

import com.arenahub.domain.group.vo.GroupName;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.Sport;

import java.time.Instant;
import java.util.UUID;

public class Group {

    private UUID id;
    private GroupName name;
    private Sport sport;
    private String description;
    private String photoUrl;
    private String pixKey;
    private GroupStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private Group() {}

    public static Group create(GroupName name, Sport sport, String description) {
        Group g = new Group();
        g.id = UUID.randomUUID();
        g.name = name;
        g.sport = sport;
        g.description = description;
        g.status = GroupStatus.ACTIVE;
        g.createdAt = Instant.now();
        g.updatedAt = Instant.now();
        return g;
    }

    public static Group reconstitute(UUID id, GroupName name, Sport sport, String description,
                                     String photoUrl, String pixKey, GroupStatus status,
                                     Instant createdAt, Instant updatedAt) {
        Group g = new Group();
        g.id = id;
        g.name = name;
        g.sport = sport;
        g.description = description;
        g.photoUrl = photoUrl;
        g.pixKey = pixKey;
        g.status = status;
        g.createdAt = createdAt;
        g.updatedAt = updatedAt;
        return g;
    }

    public void update(GroupName name, Sport sport, String description, String pixKey) {
        if (name != null) this.name = name;
        if (sport != null) this.sport = sport;
        if (description != null) this.description = description;
        if (pixKey != null) this.pixKey = pixKey;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = GroupStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == GroupStatus.ACTIVE;
    }

    public UUID getId() { return id; }
    public GroupName getName() { return name; }
    public Sport getSport() { return sport; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photoUrl; }
    public String getPixKey() { return pixKey; }
    public GroupStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
