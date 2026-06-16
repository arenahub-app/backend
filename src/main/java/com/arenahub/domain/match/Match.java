package com.arenahub.domain.match;

import com.arenahub.domain.match.vo.Location;
import com.arenahub.domain.match.vo.MatchStatus;
import com.arenahub.domain.match.vo.PresenceListStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class Match {

    private UUID id;
    private UUID groupId;
    private Instant scheduledAt;
    private Instant listClosesAt;
    private Location location;
    private int maxPlayers;
    private MatchStatus status;
    private PresenceListStatus presenceListStatus;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    private Match() {}

    public static Match create(UUID groupId, Instant scheduledAt, Location location,
                                int maxPlayers, UUID createdBy) {
        if (!scheduledAt.isAfter(Instant.now().plus(Duration.ofMinutes(30)))) {
            throw new IllegalArgumentException("scheduledAt must be at least 30 minutes in the future");
        }
        Match m = new Match();
        m.id = UUID.randomUUID();
        m.groupId = groupId;
        m.scheduledAt = scheduledAt;
        m.listClosesAt = scheduledAt.minus(Duration.ofHours(1));
        m.location = location;
        m.maxPlayers = maxPlayers;
        m.status = MatchStatus.SCHEDULED;
        m.presenceListStatus = PresenceListStatus.OPEN;
        m.createdBy = createdBy;
        m.createdAt = Instant.now();
        m.updatedAt = Instant.now();
        return m;
    }

    public static Match reconstitute(UUID id, UUID groupId, Instant scheduledAt, Instant listClosesAt,
                                      Location location, int maxPlayers, MatchStatus status,
                                      PresenceListStatus presenceListStatus, UUID createdBy,
                                      Instant createdAt, Instant updatedAt) {
        Match m = new Match();
        m.id = id;
        m.groupId = groupId;
        m.scheduledAt = scheduledAt;
        m.listClosesAt = listClosesAt;
        m.location = location;
        m.maxPlayers = maxPlayers;
        m.status = status;
        m.presenceListStatus = presenceListStatus;
        m.createdBy = createdBy;
        m.createdAt = createdAt;
        m.updatedAt = updatedAt;
        return m;
    }

    public void update(Instant newScheduledAt, Location newLocation, Integer newMaxPlayers) {
        if (status != MatchStatus.SCHEDULED || presenceListStatus != PresenceListStatus.OPEN) {
            throw new IllegalStateException("match-not-editable");
        }
        if (newScheduledAt != null) {
            if (!newScheduledAt.isAfter(Instant.now().plus(Duration.ofMinutes(30)))) {
                throw new IllegalArgumentException("scheduledAt must be at least 30 minutes in the future");
            }
            this.scheduledAt = newScheduledAt;
            this.listClosesAt = newScheduledAt.minus(Duration.ofHours(1));
        }
        if (newLocation != null) this.location = newLocation;
        if (newMaxPlayers != null) this.maxPlayers = newMaxPlayers;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == MatchStatus.CANCELLED) throw new IllegalStateException("match-already-cancelled");
        this.status = MatchStatus.CANCELLED;
        this.presenceListStatus = PresenceListStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    public void closePresenceList() {
        if (presenceListStatus == PresenceListStatus.CLOSED) {
            throw new IllegalStateException("list-already-closed");
        }
        this.presenceListStatus = PresenceListStatus.CLOSED;
        this.updatedAt = Instant.now();
    }

    public boolean isListOpen() {
        return presenceListStatus == PresenceListStatus.OPEN;
    }

    public boolean isPast() {
        return scheduledAt.isBefore(Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getGroupId() { return groupId; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getListClosesAt() { return listClosesAt; }
    public Location getLocation() { return location; }
    public int getMaxPlayers() { return maxPlayers; }
    public MatchStatus getStatus() { return status; }
    public PresenceListStatus getPresenceListStatus() { return presenceListStatus; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
