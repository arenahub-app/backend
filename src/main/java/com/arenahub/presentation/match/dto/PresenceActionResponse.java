package com.arenahub.presentation.match.dto;

public record PresenceActionResponse(
        String type,
        Object entry
) {
    public static PresenceActionResponse presence(PresenceEntryResponse entry) {
        return new PresenceActionResponse("PRESENCE", entry);
    }

    public static PresenceActionResponse waiting(WaitingEntryResponse entry) {
        return new PresenceActionResponse("WAITING", entry);
    }
}
