package com.arenahub.presentation.teamformation.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TeamFormationResponse(UUID id, UUID matchId, int numberOfTeams, String formationType,
                                     UUID confirmedBy, Instant confirmedAt, List<TeamResponse> teams) {}
