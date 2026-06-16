package com.arenahub.presentation.match.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmPresenceRequest(
        @NotBlank(message = "Action é obrigatório")
        @Pattern(regexp = "CONFIRM|DECLINE", message = "Action deve ser CONFIRM ou DECLINE")
        String action
) {}
