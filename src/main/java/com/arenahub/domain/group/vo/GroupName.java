package com.arenahub.domain.group.vo;

import java.util.Objects;

public record GroupName(String value) {

    public GroupName {
        Objects.requireNonNull(value, "Nome do grupo não pode ser nulo");
        value = value.trim();
        if (value.isBlank()) throw new IllegalArgumentException("Nome do grupo não pode ser vazio");
        if (value.length() < 3) throw new IllegalArgumentException("Nome do grupo deve ter no mínimo 3 caracteres");
        if (value.length() > 80) throw new IllegalArgumentException("Nome do grupo deve ter no máximo 80 caracteres");
    }
}
