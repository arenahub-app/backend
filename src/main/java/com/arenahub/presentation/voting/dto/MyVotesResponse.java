package com.arenahub.presentation.voting.dto;

import java.util.List;
import java.util.UUID;

public record MyVotesResponse(
        UUID votingId,
        int totalVotable,
        List<VoteResponse> votes
) {}
