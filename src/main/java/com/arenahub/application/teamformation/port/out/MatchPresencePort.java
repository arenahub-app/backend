package com.arenahub.application.teamformation.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MatchPresencePort {

    MatchPresenceSnapshot getPresenceSnapshot(UUID matchId, UUID groupId);

    record MatchPresenceSnapshot(boolean presenceListClosed, List<PlayerSnapshot> confirmedPlayers) {}

    record PlayerSnapshot(UUID memberId, UUID guestId, String userName, BigDecimal skill, String position, String role) {}
}
