package com.sushishop.shared.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikePatternTest {

    @Test
    void shouldEscapeLikeWildcardsAndEscapeCharacter() {
        assertThat(LikePattern.contains("50%_off\\")).isEqualTo("%50\\%\\_off\\\\%");
    }

    @Test
    void shouldLowercaseForCaseInsensitiveMatch() {
        assertThat(LikePattern.containsIgnoreCase("Maki_Roll")).isEqualTo("%maki\\_roll%");
    }
}
