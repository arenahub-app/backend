package com.arenahub.presentation.teamformation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TeamPlayerResponse(UUID memberId, String userName, BigDecimal skill, String position) {}
