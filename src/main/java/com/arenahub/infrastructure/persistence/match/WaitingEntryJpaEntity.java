package com.arenahub.infrastructure.persistence.match;

import com.arenahub.domain.match.WaitingEntry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "waiting_entries")
@Getter
@Setter
@NoArgsConstructor
public class WaitingEntryJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "match_id", nullable = false, columnDefinition = "uuid")
    private UUID matchId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "member_id", nullable = false, columnDefinition = "uuid")
    private UUID memberId;

    @Column(nullable = false)
    private int position;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    @Column(name = "notification_deadline")
    private Instant notificationDeadline;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WaitingEntry toDomain() {
        return WaitingEntry.reconstitute(id, matchId, groupId, memberId,
                position, notifiedAt, notificationDeadline, createdAt);
    }

    public static WaitingEntryJpaEntity fromDomain(WaitingEntry entry) {
        WaitingEntryJpaEntity e = new WaitingEntryJpaEntity();
        e.id = entry.getId();
        e.matchId = entry.getMatchId();
        e.groupId = entry.getGroupId();
        e.memberId = entry.getMemberId();
        e.position = entry.getPosition();
        e.notifiedAt = entry.getNotifiedAt();
        e.notificationDeadline = entry.getNotificationDeadline();
        e.createdAt = entry.getCreatedAt();
        return e;
    }
}
