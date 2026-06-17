package com.arenahub.presentation.match.dto;

public record PresenceActionResponse(
        String type,
        PresenceEntryResponse presenceEntry,
        WaitingEntryResponse waitingEntry,
        PendingChargeResponse pendingCharge
) {
    public static PresenceActionResponse presence(PresenceEntryResponse entry) {
        return new PresenceActionResponse("PRESENCE", entry, null, null);
    }

    public static PresenceActionResponse presence(PresenceEntryResponse entry,
                                                   PendingChargeResponse charge) {
        return new PresenceActionResponse("PRESENCE", entry, null, charge);
    }

    public static PresenceActionResponse waiting(WaitingEntryResponse entry) {
        return new PresenceActionResponse("WAITING", null, entry, null);
    }
}
