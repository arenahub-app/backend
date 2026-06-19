package com.arenahub.presentation.voting.dto;

import com.arenahub.domain.voting.vo.VotingStatus;

import java.time.Instant;
import java.util.UUID;

public record VotingResponse(
        UUID id,
        UUID groupId,
        VotingStatus status,
        UUID openedBy,
        Instant openedAt,
        Instant deadline,
        Instant closedAt,
        int totalVotable,
        int myVoteCount,
        boolean isBanned
) {}
