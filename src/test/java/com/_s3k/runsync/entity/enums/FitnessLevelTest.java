package com._s3k.runsync.entity.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FitnessLevelTest {

    @Test
    @DisplayName("fromPaceSecPerKm - 5'30\"/km보다 빠르면 상급이다")
    void fromPaceSecPerKm_advanced() {
        assertThat(FitnessLevel.fromPaceSecPerKm(329)).isEqualTo(FitnessLevel.ADVANCED);
    }

    @Test
    @DisplayName("fromPaceSecPerKm - 5'30\"/km부터 7'00\"/km까지는 중급이다")
    void fromPaceSecPerKm_intermediate() {
        assertThat(FitnessLevel.fromPaceSecPerKm(330)).isEqualTo(FitnessLevel.INTERMEDIATE);
        assertThat(FitnessLevel.fromPaceSecPerKm(420)).isEqualTo(FitnessLevel.INTERMEDIATE);
    }

    @Test
    @DisplayName("fromPaceSecPerKm - 7'00\"/km보다 느리면 초급이다")
    void fromPaceSecPerKm_beginner() {
        assertThat(FitnessLevel.fromPaceSecPerKm(421)).isEqualTo(FitnessLevel.BEGINNER);
    }
}
