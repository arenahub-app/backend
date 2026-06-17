package com.arenahub.presentation.teamformation.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TeamResponse(UUID id, String name, BigDecimal averageSkill, int playerCount, List<TeamPlayerResponse> players) {}
