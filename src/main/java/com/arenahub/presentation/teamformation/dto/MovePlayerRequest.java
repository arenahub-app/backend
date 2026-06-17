package com.arenahub.presentation.teamformation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MovePlayerRequest(@NotNull UUID memberId, @NotNull UUID toTeamId) {}
