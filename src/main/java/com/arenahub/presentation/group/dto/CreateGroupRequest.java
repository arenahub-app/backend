package com.arenahub.presentation.group.dto;

import com.arenahub.domain.group.vo.Sport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
        @NotBlank @Size(min = 3, max = 80) String name,
        @NotNull Sport sport,
        @Size(max = 500) String description
) {}
