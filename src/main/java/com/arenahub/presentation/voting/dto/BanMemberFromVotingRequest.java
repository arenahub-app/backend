package com.arenahub.presentation.voting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BanMemberFromVotingRequest(
        @NotNull(message = "O ID do membro é obrigatório")
        UUID memberId,

        @NotBlank(message = "O motivo é obrigatório")
        String reason
) {}
