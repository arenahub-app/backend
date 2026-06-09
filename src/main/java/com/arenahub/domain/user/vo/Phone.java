package com.arenahub.domain.user.vo;

import java.util.Objects;

public record Phone(String value) {

    public Phone {
        Objects.requireNonNull(value, "Telefone não pode ser nulo");
        value = value.replaceAll("\\D", "");
        if (value.length() < 10 || value.length() > 11)
            throw new IllegalArgumentException("Telefone deve ter 10 ou 11 dígitos");
    }
}
