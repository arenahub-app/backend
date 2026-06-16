package com.arenahub.presentation.match.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateMatchRequest(
        Instant scheduledAt,

        @Size(max = 200, message = "Nome do local deve ter no máximo 200 caracteres")
        String locationName,

        String locationAddress,

        @Min(value = 2, message = "Número máximo de jogadores deve ser pelo menos 2")
        Integer maxPlayers
) {}
