package com.arenahub.presentation.teamformation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MovePlayerRequest(UUID memberId, UUID guestId, @NotNull UUID toTeamId) {}
