package com.arenahub.domain.voting;

import com.arenahub.domain.voting.vo.Stars;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class StarsTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    void valid_range_accepted(int value) {
        assertThatNoException().isThrownBy(() -> new Stars(value));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7, -1, 100})
    void invalid_range_throws(int value) {
        assertThatThrownBy(() -> new Stars(value))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
