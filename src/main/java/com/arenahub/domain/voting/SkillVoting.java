package com.arenahub.domain.voting;

import com.arenahub.domain.voting.vo.VotingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SkillVoting {

    private UUID id;
    private UUID groupId;
    private VotingStatus status;
    private UUID openedBy;
    private Instant openedAt;
    private Instant deadline;
    private Instant closedAt;

    private SkillVoting() {}

    public static SkillVoting open(UUID groupId, Instant deadline, UUID openedBy) {
        if (!deadline.isAfter(Instant.now().plusSeconds(30 * 60))) {
            throw new IllegalArgumentException("O prazo deve ser pelo menos 30 minutos no futuro");
        }
        SkillVoting sv = new SkillVoting();
        sv.id = UUID.randomUUID();
        sv.groupId = groupId;
        sv.status = VotingStatus.OPEN;
        sv.openedBy = openedBy;
        sv.openedAt = Instant.now();
        sv.deadline = deadline;
        return sv;
    }

    public static SkillVoting reconstitute(UUID id, UUID groupId, VotingStatus status,
                                           UUID openedBy, Instant openedAt,
                                           Instant deadline, Instant closedAt) {
        SkillVoting sv = new SkillVoting();
        sv.id = id;
        sv.groupId = groupId;
        sv.status = status;
        sv.openedBy = openedBy;
        sv.openedAt = openedAt;
        sv.deadline = deadline;
        sv.closedAt = closedAt;
        return sv;
    }

    public void close(Instant now) {
        if (this.status == VotingStatus.CLOSED) {
            throw new IllegalStateException("Votação já está encerrada");
        }
        this.status = VotingStatus.CLOSED;
        this.closedAt = now;
    }

    public boolean isOpen() {
        return this.status == VotingStatus.OPEN;
    }

    public UUID getId() { return id; }
    public UUID getGroupId() { return groupId; }
    public VotingStatus getStatus() { return status; }
    public UUID getOpenedBy() { return openedBy; }
    public Instant getOpenedAt() { return openedAt; }
    public Instant getDeadline() { return deadline; }
    public Instant getClosedAt() { return closedAt; }
}
