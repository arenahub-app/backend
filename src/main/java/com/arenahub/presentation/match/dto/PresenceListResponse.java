package com.arenahub.presentation.match.dto;

import java.util.List;

public record PresenceListResponse(
        List<PresenceEntryResponse> confirmed,
        List<PresenceEntryResponse> declined,
        List<WaitingEntryResponse> waiting
) {}
