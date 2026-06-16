package com.arenahub.domain.group.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Skill(BigDecimal value) {

    public static final Skill DEFAULT = new Skill(new BigDecimal("3.0"));

    public Skill {
        Objects.requireNonNull(value, "Skill não pode ser nulo");
        value = value.setScale(1, RoundingMode.HALF_UP);
        if (value.compareTo(BigDecimal.ONE) < 0 || value.compareTo(new BigDecimal("6.0")) > 0) {
            throw new IllegalArgumentException("Skill deve estar entre 1.0 e 6.0");
        }
    }

    public static Skill of(double value) {
        return new Skill(BigDecimal.valueOf(value));
    }
}
