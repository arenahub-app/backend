package com.arenahub.domain.group.vo;

public enum GroupRole {
    OWNER,
    ADMIN,
    PLAYER,
    REFEREE;

    public boolean isAtLeast(GroupRole required) {
        return switch (required) {
            case OWNER -> this == OWNER;
            case ADMIN -> this == OWNER || this == ADMIN;
            default -> true; // PLAYER/REFEREE: any member satisfies
        };
    }
}
