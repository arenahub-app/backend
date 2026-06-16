package com.arenahub.infrastructure.persistence.match;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "presence_ban_history")
@Getter
@Setter
@NoArgsConstructor
public class PresenceBanHistoryJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "member_id", nullable = false, columnDefinition = "uuid")
    private UUID memberId;

    @Column(nullable = false, length = 10)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "performed_by", nullable = false, columnDefinition = "uuid")
    private UUID performedBy;

    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    public static PresenceBanHistoryJpaEntity ban(UUID groupId, UUID memberId,
                                                   String reason, UUID performedBy) {
        PresenceBanHistoryJpaEntity e = new PresenceBanHistoryJpaEntity();
        e.id = UUID.randomUUID();
        e.groupId = groupId;
        e.memberId = memberId;
        e.action = "BANNED";
        e.reason = reason;
        e.performedBy = performedBy;
        e.performedAt = Instant.now();
        return e;
    }

    public static PresenceBanHistoryJpaEntity unban(UUID groupId, UUID memberId, UUID performedBy) {
        PresenceBanHistoryJpaEntity e = new PresenceBanHistoryJpaEntity();
        e.id = UUID.randomUUID();
        e.groupId = groupId;
        e.memberId = memberId;
        e.action = "UNBANNED";
        e.performedBy = performedBy;
        e.performedAt = Instant.now();
        return e;
    }
}
