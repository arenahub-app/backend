package com.arenahub.domain.voting.vo;

public record Stars(int value) {

    public Stars {
        if (value < 1 || value > 6) {
            throw new IllegalArgumentException("Stars deve estar entre 1 e 6");
        }
    }
}
