package com.arenahub.presentation.teamformation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateFormationRequest(@NotNull @Min(2) Integer numberOfTeams) {}
