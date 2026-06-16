package com.arenahub.domain.match.vo;

public record Location(String name, String address) {

    public Location {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Location name is required");
        if (name.length() > 200) throw new IllegalArgumentException("Location name must be at most 200 characters");
    }
}
