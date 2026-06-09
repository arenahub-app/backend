package com.arenahub.domain.user.vo;

import java.util.Objects;

public record Name(String value) {

    public Name {
        Objects.requireNonNull(value, "Nome não pode ser nulo");
        value = value.trim();
        if (value.length() < 2 || value.length() > 100)
            throw new IllegalArgumentException("Nome deve ter entre 2 e 100 caracteres");
    }
}
