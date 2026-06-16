package com.arenahub.application.group.port.in;

import com.arenahub.presentation.group.dto.GroupSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ListGroupsUseCase {

    List<GroupSummaryResponse> execute(UUID userId);
}
