package com.arenahub.infrastructure.persistence.match;

import com.arenahub.domain.match.PresenceEntry;
import com.arenahub.domain.match.vo.JustificationStatus;
import com.arenahub.domain.match.vo.PresenceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "presence_entries")
@Getter
@Setter
@NoArgsConstructor
public class PresenceEntryJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "match_id", nullable = false, columnDefinition = "uuid")
    private UUID matchId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "member_id", nullable = false, columnDefinition = "uuid")
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PresenceStatus status;

    @Column(columnDefinition = "TEXT")
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(name = "justification_status", length = 10)
    private JustificationStatus justificationStatus;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PresenceEntry toDomain() {
        return PresenceEntry.reconstitute(id, matchId, groupId, memberId,
                status, justification, justificationStatus, confirmedAt);
    }

    public static PresenceEntryJpaEntity fromDomain(PresenceEntry entry) {
        PresenceEntryJpaEntity e = new PresenceEntryJpaEntity();
        e.id = entry.getId();
        e.matchId = entry.getMatchId();
        e.groupId = entry.getGroupId();
        e.memberId = entry.getMemberId();
        e.status = entry.getStatus();
        e.justification = entry.getJustification();
        e.justificationStatus = entry.getJustificationStatus();
        e.confirmedAt = entry.getConfirmedAt();
        e.updatedAt = Instant.now();
        return e;
    }
}
