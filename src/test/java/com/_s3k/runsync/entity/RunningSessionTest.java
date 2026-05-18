package com._s3k.runsync.entity;

import com._s3k.runsync.domain.run.exception.RunSessionErrorCode;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import com._s3k.runsync.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunningSessionTest {

    @Test
    @DisplayName("세션 생성 시 초기 상태가 ACTIVE이다")
    void createSession_initialStatusIsActive() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);

        // when
        RunningSession session = RunningSession.of(user, LocalDateTime.now());

        // then
        assertThat(session.getStatus()).isEqualTo(RunningSessionStatus.ACTIVE);
    }

    @Test
    @DisplayName("위치 업데이트 시 lastLocation, totalDistance, currentDurationTime이 갱신된다")
    void updateLocation() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());

        // when
        session.updateLocation(36.3321, 127.4562, BigDecimal.valueOf(2.34), 754);

        // then
        assertThat(session.getLastLocation().getY()).isEqualTo(36.3321);  // Y = latitude
        assertThat(session.getLastLocation().getX()).isEqualTo(127.4562); // X = longitude
        assertThat(session.getTotalDistance()).isEqualByComparingTo(BigDecimal.valueOf(2.34));
        assertThat(session.getCurrentDurationTime()).isEqualTo(754);
    }

    @Test
    @DisplayName("complete 시 상태가 COMPLETED로 변경되고 종료 시간과 총 거리가 기록된다")
    void complete() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(30);

        // when
        session.complete(endTime, BigDecimal.valueOf(5.0));

        // then
        assertThat(session.getStatus()).isEqualTo(RunningSessionStatus.COMPLETED);
        assertThat(session.getEndTime()).isEqualTo(endTime);
        assertThat(session.getTotalDistance()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
    }

    @Test
    @DisplayName("pause 시 상태가 PAUSED로 변경된다")
    void pause() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());

        // when
        session.pause();

        // then
        assertThat(session.getStatus()).isEqualTo(RunningSessionStatus.PAUSED);
    }

    @Test
    @DisplayName("resume 시 상태가 ACTIVE로 변경된다")
    void resume() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());
        session.pause();

        // when
        session.resume();

        // then
        assertThat(session.getStatus()).isEqualTo(RunningSessionStatus.ACTIVE);
    }

    @Test
    @DisplayName("createRecord 시 세션 정보로 RunRecord가 생성된다")
    void createRecord() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        LocalDateTime startTime = LocalDateTime.now();
        RunningSession session = RunningSession.of(user, startTime);
        session.updateLocation(36.3321, 127.4562, BigDecimal.valueOf(2.0), 600);

        // when
        RunRecord record = session.createRecord(BigDecimal.valueOf(5.0));

        // then
        assertThat(record.getDurationSeconds()).isEqualTo(600);
        assertThat(record.getStartTime()).isEqualTo(startTime);
        assertThat(record.getDistance()).isEqualByComparingTo(BigDecimal.valueOf(5.0));
    }

    @Test
    @DisplayName("validateOwner - 소유자가 맞으면 예외 없이 통과한다")
    void validateOwner_success() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());
        ReflectionTestUtils.setField(session, "userId", 1L);

        // when & then
        session.validateOwner(1L);
    }

    @Test
    @DisplayName("validateOwner - 다른 유저면 SESSION_NOT_OWNER 예외 발생")
    void validateOwner_notOwner() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());
        ReflectionTestUtils.setField(session, "userId", 1L);

        // when & then
        assertThatThrownBy(() -> session.validateOwner(2L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_OWNER));
    }

    @Test
    @DisplayName("validateActive - ACTIVE 상태면 예외 없이 통과한다")
    void validateActive_success() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());

        // when & then
        session.validateActive();
    }

    @Test
    @DisplayName("validateActive - ACTIVE 아니면 SESSION_NOT_ACTIVE 예외 발생")
    void validateActive_notActive() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());
        session.complete(LocalDateTime.now().plusMinutes(30), BigDecimal.valueOf(5.0));

        // when & then
        assertThatThrownBy(session::validateActive)
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_ACTIVE));
    }

    @Test
    @DisplayName("validateCompleted - COMPLETED 상태면 예외 없이 통과한다")
    void validateCompleted_success() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());
        session.complete(LocalDateTime.now().plusMinutes(30), BigDecimal.valueOf(5.0));

        // when & then
        session.validateCompleted();
    }

    @Test
    @DisplayName("validateCompleted - COMPLETED 아니면 SESSION_NOT_COMPLETED 예외 발생")
    void validateCompleted_notCompleted() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now());

        // when & then
        assertThatThrownBy(session::validateCompleted)
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_COMPLETED));
    }
}
