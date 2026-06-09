package com.arenahub.domain.user.vo;

import java.util.Objects;

public record Email(String value) {

    private static final String REGEX =
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    public Email {
        Objects.requireNonNull(value, "Email não pode ser nulo");
        value = value.toLowerCase().trim();
        if (value.isBlank()) throw new IllegalArgumentException("Email não pode ser vazio");
        if (value.length() > 254) throw new IllegalArgumentException("Email muito longo");
        if (!value.matches(REGEX)) throw new IllegalArgumentException("Email inválido: " + value);
    }
}
