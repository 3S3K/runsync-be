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

    @Test
    @DisplayName("updateProfileImage - 새 사진 주소가 있으면 바꾼다")
    void updateProfileImage_changes() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", "http://old.example/a.jpg");

        // when
        user.updateProfileImage("http://new.example/b.jpg");

        // then
        assertThat(user.getProfileImage()).isEqualTo("http://new.example/b.jpg");
    }

    @Test
    @DisplayName("updateProfileImage - 사진을 받지 못했으면(null·빈 값) 기존 사진을 유지한다")
    void updateProfileImage_keepsWhenMissing() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", "http://old.example/a.jpg");

        // when
        user.updateProfileImage(null);
        user.updateProfileImage(" ");

        // then
        assertThat(user.getProfileImage()).isEqualTo("http://old.example/a.jpg");
    }
}
