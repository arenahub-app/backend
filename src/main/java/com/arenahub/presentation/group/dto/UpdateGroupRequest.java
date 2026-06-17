package com.arenahub.presentation.group.dto;

import com.arenahub.domain.group.vo.Sport;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateGroupRequest(
        @Size(min = 3, max = 80) String name,
        Sport sport,
        @Size(max = 500) String description,
        String pixKey,
        @DecimalMin(value = "0") BigDecimal matchFee
) {}
