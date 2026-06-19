package com.arenahub.presentation.voting.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record OpenVotingRequest(
        @NotNull(message = "O prazo é obrigatório")
        @Future(message = "O prazo deve ser uma data futura")
        Instant deadline
) {}
