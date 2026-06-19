package com.arenahub.presentation.voting.dto;

import com.arenahub.domain.voting.vo.VotingStatus;

import java.time.Instant;
import java.util.UUID;

public record VotingSummaryResponse(
        UUID id,
        VotingStatus status,
        Instant openedAt,
        Instant deadline,
        Instant closedAt
) {}
