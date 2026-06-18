package com.arenahub.presentation.match.dto;

import com.arenahub.domain.group.vo.PlayerPosition;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddGuestRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @DecimalMin("1.0") @DecimalMax("5.0") BigDecimal skill,
        @NotNull PlayerPosition position
) {}
