package com.arenahub.infrastructure.persistence.match;

import com.arenahub.application.match.port.out.MatchRepository;
import com.arenahub.domain.match.Match;
import com.arenahub.domain.match.PresenceEntry;
import com.arenahub.domain.match.WaitingEntry;
import com.arenahub.domain.match.vo.PresenceListStatus;
import com.arenahub.domain.match.vo.PresenceStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MatchRepositoryAdapter implements MatchRepository {

    private final MatchJpaRepository matchRepo;
    private final PresenceEntryJpaRepository presenceRepo;
    private final WaitingEntryJpaRepository waitingRepo;

    public MatchRepositoryAdapter(MatchJpaRepository matchRepo,
                                   PresenceEntryJpaRepository presenceRepo,
                                   WaitingEntryJpaRepository waitingRepo) {
        this.matchRepo = matchRepo;
        this.presenceRepo = presenceRepo;
        this.waitingRepo = waitingRepo;
    }

    @Override
    public Match save(Match match) {
        return matchRepo.save(MatchJpaEntity.fromDomain(match)).toDomain();
    }

    @Override
    public Optional<Match> findById(UUID id) {
        return matchRepo.findById(id).map(MatchJpaEntity::toDomain);
    }

    @Override
    public Optional<Match> findByIdAndGroupId(UUID id, UUID groupId) {
        return matchRepo.findByIdAndGroupId(id, groupId).map(MatchJpaEntity::toDomain);
    }

    @Override
    public List<Match> findUpcomingByGroupId(UUID groupId) {
        return matchRepo.findByGroupIdAndScheduledAtAfterOrderByScheduledAtAsc(groupId, Instant.now())
                .stream().map(MatchJpaEntity::toDomain).toList();
    }

    @Override
    public List<Match> findPastByGroupId(UUID groupId) {
        return matchRepo.findByGroupIdAndScheduledAtBeforeOrderByScheduledAtDesc(groupId, Instant.now())
                .stream().map(MatchJpaEntity::toDomain).toList();
    }

    @Override
    public List<Match> findAllByGroupId(UUID groupId) {
        return matchRepo.findByGroupIdOrderByScheduledAtDesc(groupId)
                .stream().map(MatchJpaEntity::toDomain).toList();
    }

    @Override
    public List<Match> findByPresenceListStatusAndListClosesAtBefore(PresenceListStatus status, Instant cutoff) {
        return matchRepo.findByPresenceListStatusAndListClosesAtBefore(status, cutoff)
                .stream().map(MatchJpaEntity::toDomain).toList();
    }

    @Override
    public PresenceEntry savePresenceEntry(PresenceEntry entry) {
        return presenceRepo.save(PresenceEntryJpaEntity.fromDomain(entry)).toDomain();
    }

    @Override
    public void deletePresenceEntry(UUID id) {
        presenceRepo.deleteById(id);
    }

    @Override
    public Optional<PresenceEntry> findPresenceEntry(UUID matchId, UUID memberId) {
        return presenceRepo.findByMatchIdAndMemberId(matchId, memberId)
                .map(PresenceEntryJpaEntity::toDomain);
    }

    @Override
    public List<PresenceEntry> findPresenceEntriesByMatchId(UUID matchId) {
        return presenceRepo.findByMatchId(matchId).stream()
                .map(PresenceEntryJpaEntity::toDomain).toList();
    }

    @Override
    public List<PresenceEntry> findPresenceEntriesByMatchIdAndStatus(UUID matchId, PresenceStatus status) {
        return presenceRepo.findByMatchIdAndStatus(matchId, status).stream()
                .map(PresenceEntryJpaEntity::toDomain).toList();
    }

    @Override
    public long countConfirmedByMatchId(UUID matchId) {
        return presenceRepo.countByMatchIdAndStatus(matchId, PresenceStatus.CONFIRMED);
    }

    @Override
    public WaitingEntry saveWaitingEntry(WaitingEntry entry) {
        return waitingRepo.save(WaitingEntryJpaEntity.fromDomain(entry)).toDomain();
    }

    @Override
    public void deleteWaitingEntry(UUID id) {
        waitingRepo.deleteById(id);
    }

    @Override
    public Optional<WaitingEntry> findWaitingEntry(UUID matchId, UUID memberId) {
        return waitingRepo.findByMatchIdAndMemberId(matchId, memberId)
                .map(WaitingEntryJpaEntity::toDomain);
    }

    @Override
    public Optional<WaitingEntry> findFirstWaitingEntry(UUID matchId) {
        return waitingRepo.findFirstByMatchIdOrderByPositionAsc(matchId)
                .map(WaitingEntryJpaEntity::toDomain);
    }

    @Override
    public List<WaitingEntry> findWaitingEntriesByMatchId(UUID matchId) {
        return waitingRepo.findByMatchIdOrderByPositionAsc(matchId).stream()
                .map(WaitingEntryJpaEntity::toDomain).toList();
    }

    @Override
    public long countWaitingByMatchId(UUID matchId) {
        return waitingRepo.countByMatchId(matchId);
    }
}
