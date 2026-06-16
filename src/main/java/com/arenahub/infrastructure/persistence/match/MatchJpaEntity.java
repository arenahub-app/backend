package com.arenahub.infrastructure.persistence.match;

import com.arenahub.domain.match.Match;
import com.arenahub.domain.match.vo.Location;
import com.arenahub.domain.match.vo.MatchStatus;
import com.arenahub.domain.match.vo.PresenceListStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class MatchJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "list_closes_at", nullable = false)
    private Instant listClosesAt;

    @Column(name = "location_name", nullable = false, length = 200)
    private String locationName;

    @Column(name = "location_address", columnDefinition = "TEXT")
    private String locationAddress;

    @Column(name = "max_players", nullable = false)
    private int maxPlayers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private MatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "presence_list_status", nullable = false, length = 10)
    private PresenceListStatus presenceListStatus;

    @Column(name = "created_by", nullable = false, columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Match toDomain() {
        return Match.reconstitute(id, groupId, scheduledAt, listClosesAt,
                new Location(locationName, locationAddress),
                maxPlayers, status, presenceListStatus, createdBy, createdAt, updatedAt);
    }

    public static MatchJpaEntity fromDomain(Match match) {
        MatchJpaEntity e = new MatchJpaEntity();
        e.id = match.getId();
        e.groupId = match.getGroupId();
        e.scheduledAt = match.getScheduledAt();
        e.listClosesAt = match.getListClosesAt();
        e.locationName = match.getLocation().name();
        e.locationAddress = match.getLocation().address();
        e.maxPlayers = match.getMaxPlayers();
        e.status = match.getStatus();
        e.presenceListStatus = match.getPresenceListStatus();
        e.createdBy = match.getCreatedBy();
        e.createdAt = match.getCreatedAt();
        e.updatedAt = match.getUpdatedAt();
        return e;
    }
}
