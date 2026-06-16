package com.arenahub.presentation.match.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateMatchRequest(
        @NotNull(message = "Data da partida é obrigatória")
        Instant scheduledAt,

        @NotBlank(message = "Nome do local é obrigatório")
        @Size(max = 200, message = "Nome do local deve ter no máximo 200 caracteres")
        String locationName,

        String locationAddress,

        @NotNull(message = "Número máximo de jogadores é obrigatório")
        @Min(value = 2, message = "Número máximo de jogadores deve ser pelo menos 2")
        Integer maxPlayers
) {}
