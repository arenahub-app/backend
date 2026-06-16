package com.arenahub.presentation.match.dto;

import jakarta.validation.constraints.NotBlank;

public record BanMemberRequest(
        @NotBlank(message = "Motivo do banimento é obrigatório")
        String reason
) {}
