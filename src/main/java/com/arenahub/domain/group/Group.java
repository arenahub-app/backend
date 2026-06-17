package com.arenahub.domain.group;

import com.arenahub.domain.group.vo.GroupName;
import com.arenahub.domain.group.vo.GroupStatus;
import com.arenahub.domain.group.vo.Sport;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Group {

    private UUID id;
    private GroupName name;
    private Sport sport;
    private String description;
    private String photoUrl;
    private String pixKey;
    private BigDecimal matchFee;
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
                                     String photoUrl, String pixKey, BigDecimal matchFee,
                                     GroupStatus status, Instant createdAt, Instant updatedAt) {
        Group g = new Group();
        g.id = id;
        g.name = name;
        g.sport = sport;
        g.description = description;
        g.photoUrl = photoUrl;
        g.pixKey = pixKey;
        g.matchFee = matchFee;
        g.status = status;
        g.createdAt = createdAt;
        g.updatedAt = updatedAt;
        return g;
    }

    public void update(GroupName name, Sport sport, String description,
                       String pixKey, BigDecimal matchFee) {
        if (name != null) this.name = name;
        if (sport != null) this.sport = sport;
        if (description != null) this.description = description;
        if (pixKey != null) this.pixKey = pixKey;
        if (matchFee != null) {
            this.matchFee = matchFee.compareTo(BigDecimal.ZERO) > 0 ? matchFee : null;
        }
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = GroupStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == GroupStatus.ACTIVE;
    }

    public boolean requiresPayment() {
        return matchFee != null && matchFee.compareTo(BigDecimal.ZERO) > 0;
    }

    public UUID getId() { return id; }
    public GroupName getName() { return name; }
    public Sport getSport() { return sport; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photoUrl; }
    public String getPixKey() { return pixKey; }
    public BigDecimal getMatchFee() { return matchFee; }
    public GroupStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
