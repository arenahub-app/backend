package com.arenahub.presentation.voting.dto;

import com.arenahub.domain.voting.vo.VotingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VotingResultsResponse(
        UUID votingId,
        VotingStatus status,
        Instant closedAt,
        List<MemberResultResponse> members
) {}
