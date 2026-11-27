package com._s3k.runsync.entity;

import com._s3k.runsync.entity.enums.Gender;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.entity.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("calculateAge - 생일이 지났으면 올해 나이를 반환한다")
    void calculateAge_afterBirthday() {
        User user = userBornOn(LocalDate.of(2002, 5, 1));

        assertThat(user.calculateAge(LocalDate.of(2026, 11, 27))).isEqualTo(24);
    }

    @Test
    @DisplayName("calculateAge - 생일이 아직 지나지 않았으면 한 살 적다")
    void calculateAge_beforeBirthday() {
        User user = userBornOn(LocalDate.of(2002, 12, 31));

        assertThat(user.calculateAge(LocalDate.of(2026, 11, 27))).isEqualTo(23);
    }

    @Test
    @DisplayName("calculateAge - 생일 당일에 한 살이 오른다")
    void calculateAge_onBirthday() {
        User user = userBornOn(LocalDate.of(2002, 11, 27));

        assertThat(user.calculateAge(LocalDate.of(2026, 11, 26))).isEqualTo(23);
        assertThat(user.calculateAge(LocalDate.of(2026, 11, 27))).isEqualTo(24);
    }

    @Test
    @DisplayName("calculateAge - 생년월일이 없으면 null")
    void calculateAge_withoutBirthDate() {
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);

        assertThat(user.calculateAge(LocalDate.of(2026, 11, 27))).isNull();
    }

    private User userBornOn(LocalDate birthDate) {
        return User.of("nickname", "kakaoId", Provider.KAKAO, null, Role.USER, birthDate, Gender.MALE);
    }
}
