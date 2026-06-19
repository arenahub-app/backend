package com.arenahub.presentation.voting.dto;

import java.time.Instant;
import java.util.UUID;

public record VoteResponse(
        UUID targetMemberId,
        String targetUserName,
        int stars,
        Instant votedAt
) {}
