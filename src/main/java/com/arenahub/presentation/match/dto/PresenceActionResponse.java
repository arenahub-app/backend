package com.arenahub.presentation.match.dto;

public record PresenceActionResponse(
        String type,
        PresenceEntryResponse presenceEntry,
        WaitingEntryResponse waitingEntry
) {
    public static PresenceActionResponse presence(PresenceEntryResponse entry) {
        return new PresenceActionResponse("PRESENCE", entry, null);
    }

    public static PresenceActionResponse waiting(WaitingEntryResponse entry) {
        return new PresenceActionResponse("WAITING", null, entry);
    }
}
