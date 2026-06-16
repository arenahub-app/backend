package com.arenahub.application.match.port.out;

import com.arenahub.domain.match.Match;
import com.arenahub.domain.match.PresenceEntry;
import com.arenahub.domain.match.WaitingEntry;
import com.arenahub.domain.match.vo.PresenceListStatus;
import com.arenahub.domain.match.vo.PresenceStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository {

    Match save(Match match);

    Optional<Match> findById(UUID id);

    Optional<Match> findByIdAndGroupId(UUID id, UUID groupId);

    List<Match> findUpcomingByGroupId(UUID groupId);

    List<Match> findPastByGroupId(UUID groupId);

    List<Match> findAllByGroupId(UUID groupId);

    List<Match> findByPresenceListStatusAndListClosesAtBefore(PresenceListStatus status, Instant cutoff);

    PresenceEntry savePresenceEntry(PresenceEntry entry);

    void deletePresenceEntry(UUID id);

    Optional<PresenceEntry> findPresenceEntry(UUID matchId, UUID memberId);

    List<PresenceEntry> findPresenceEntriesByMatchId(UUID matchId);

    List<PresenceEntry> findPresenceEntriesByMatchIdAndStatus(UUID matchId, PresenceStatus status);

    long countConfirmedByMatchId(UUID matchId);

    WaitingEntry saveWaitingEntry(WaitingEntry entry);

    void deleteWaitingEntry(UUID id);

    Optional<WaitingEntry> findWaitingEntry(UUID matchId, UUID memberId);

    Optional<WaitingEntry> findFirstWaitingEntry(UUID matchId);

    List<WaitingEntry> findWaitingEntriesByMatchId(UUID matchId);

    long countWaitingByMatchId(UUID matchId);
}
