package com.arenahub.presentation.voting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MemberResultResponse(
        UUID memberId,
        String userName,
        String photoUrl,
        BigDecimal previousSkill,
        BigDecimal newSkill,
        int voteCount
) {}
