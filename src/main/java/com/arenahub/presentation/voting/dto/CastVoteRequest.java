package com.arenahub.presentation.voting.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CastVoteRequest(
        @NotNull(message = "Stars é obrigatório")
        @Min(value = 1, message = "Stars deve ser pelo menos 1")
        @Max(value = 6, message = "Stars deve ser no máximo 6")
        Integer stars
) {}
