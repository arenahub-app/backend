package com.arenahub.infrastructure.persistence.match;

import com.arenahub.domain.group.vo.PlayerPosition;
import com.arenahub.domain.match.MatchGuest;
import com.arenahub.domain.match.vo.GuestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match_guests")
@Getter
@Setter
@NoArgsConstructor
public class MatchGuestJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "match_id", nullable = false, columnDefinition = "uuid")
    private UUID matchId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlayerPosition position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GuestStatus status;

    @Column(name = "added_by", nullable = false, columnDefinition = "uuid")
    private UUID addedBy;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public MatchGuest toDomain() {
        return MatchGuest.reconstitute(id, matchId, groupId, name, skill, position,
                status, addedBy, confirmedAt, createdAt);
    }

    public static MatchGuestJpaEntity fromDomain(MatchGuest guest) {
        MatchGuestJpaEntity e = new MatchGuestJpaEntity();
        e.id = guest.getId();
        e.matchId = guest.getMatchId();
        e.groupId = guest.getGroupId();
        e.name = guest.getName();
        e.skill = guest.getSkill();
        e.position = guest.getPosition();
        e.status = guest.getStatus();
        e.addedBy = guest.getAddedBy();
        e.confirmedAt = guest.getConfirmedAt();
        e.createdAt = guest.getCreatedAt();
        return e;
    }
}
