package com.arenahub.infrastructure.persistence.voting;

import com.arenahub.domain.voting.VotingBan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "voting_bans")
@Getter
@Setter
@NoArgsConstructor
public class VotingBanJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "voting_id", nullable = false, columnDefinition = "uuid")
    private UUID votingId;

    @Column(name = "group_id", nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(name = "member_id", nullable = false, columnDefinition = "uuid")
    private UUID memberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "banned_by", nullable = false, columnDefinition = "uuid")
    private UUID bannedBy;

    @Column(name = "banned_at", nullable = false, updatable = false)
    private Instant bannedAt;

    public VotingBan toDomain() {
        return VotingBan.reconstitute(id, votingId, groupId, memberId, reason, bannedBy, bannedAt);
    }

    public static VotingBanJpaEntity fromDomain(VotingBan ban) {
        VotingBanJpaEntity e = new VotingBanJpaEntity();
        e.id = ban.getId();
        e.votingId = ban.getVotingId();
        e.groupId = ban.getGroupId();
        e.memberId = ban.getMemberId();
        e.reason = ban.getReason();
        e.bannedBy = ban.getBannedBy();
        e.bannedAt = ban.getBannedAt();
        return e;
    }
}
