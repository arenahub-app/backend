package com.arenahub.application.match.port.out;

import com.arenahub.domain.match.MatchGuest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuestRepository {

    MatchGuest save(MatchGuest guest);

    Optional<MatchGuest> findById(UUID id);

    Optional<MatchGuest> findByIdAndMatchId(UUID id, UUID matchId);

    List<MatchGuest> findByMatchId(UUID matchId);

    List<MatchGuest> findConfirmedByMatchId(UUID matchId);

    long countOccupiedByMatchId(UUID matchId);

    void delete(UUID id);
}
