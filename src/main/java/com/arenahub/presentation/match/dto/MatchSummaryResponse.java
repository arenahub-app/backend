package com.arenahub.presentation.match.dto;

import com.arenahub.domain.match.vo.MatchStatus;
import com.arenahub.domain.match.vo.PresenceListStatus;

import java.time.Instant;
import java.util.UUID;

public record MatchSummaryResponse(
        UUID id,
        Instant scheduledAt,
        Instant listClosesAt,
        String locationName,
        String locationAddress,
        int maxPlayers,
        MatchStatus status,
        PresenceListStatus presenceListStatus,
        long confirmedCount,
        long waitingCount
) {}
