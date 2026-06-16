package com.arenahub.presentation.match.dto;

import com.arenahub.domain.match.vo.MatchStatus;
import com.arenahub.domain.match.vo.PresenceListStatus;
import com.arenahub.domain.match.vo.PresenceStatus;

import java.time.Instant;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        UUID groupId,
        Instant scheduledAt,
        Instant listClosesAt,
        String locationName,
        String locationAddress,
        int maxPlayers,
        MatchStatus status,
        PresenceListStatus presenceListStatus,
        UUID createdBy,
        Instant createdAt,
        String myPresenceStatus,
        long confirmedCount,
        long waitingCount
) {}
