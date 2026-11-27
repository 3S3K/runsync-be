package com._s3k.runsync.domain.course.service;

import com._s3k.runsync.domain.course.dto.response.RunnerRecentRunRes;
import com._s3k.runsync.domain.course.dto.response.RunnerSummaryRes;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.enums.FitnessLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RunnerSummaryServiceTest {

    private static final Long USER_ID = 12L;

    @Mock
    private RunRecordRepository runRecordRepository;

    // 한국 시간 2026-11-27 15:00
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-27T06:00:00Z"), ZoneId.of("Asia/Seoul"));

    private RunnerSummaryService runnerSummaryService;

    @BeforeEach
    void setUp() {
        runnerSummaryService = new RunnerSummaryService(runRecordRepository, clock);
    }

    @Test
    @DisplayName("최근 기록의 평균 페이스·심박·케이던스·거리와 체력 수준을 계산한다")
    void getRunnerSummaryByUserId_success() {
        // given: 총 15km, 5700초 → 380초/km
        givenRecentRecords(List.of(
                record("5.00", 1900, LocalDate.of(2026, 11, 26), 150, 160),
                record("6.00", 2280, LocalDate.of(2026, 11, 24), null, 170),
                record("4.00", 1520, LocalDate.of(2026, 11, 20), 160, 0)
        ));
        givenWeeklyDistance("11.00");

        // when
        RunnerSummaryRes result = runnerSummaryService.getRunnerSummaryByUserId(USER_ID);

        // then
        assertThat(result.getRecordCount()).isEqualTo(3);
        assertThat(result.getAveragePaceSecPerKm()).isEqualTo(380);
        assertThat(result.getAverageDistanceKm()).isEqualTo(5.0);
        assertThat(result.getWeeklyDistanceKm()).isEqualTo(11.0);
        assertThat(result.getFitnessLevel()).isEqualTo(FitnessLevel.INTERMEDIATE);
        // 값이 없거나 0인 기록은 평균에서 뺀다
        assertThat(result.getAverageHeartRate()).isEqualTo(155);
        assertThat(result.getAverageCadence()).isEqualTo(165);
    }

    @Test
    @DisplayName("최근 러닝 목록은 오래된 순으로 돌려준다")
    void getRunnerSummaryByUserId_recentRunsOldestFirst() {
        // given: 조회 결과는 최신순
        givenRecentRecords(List.of(
                record("5.00", 1900, LocalDate.of(2026, 11, 26), null, null),
                record("6.25", 2280, LocalDate.of(2026, 11, 24), null, null),
                record("4.00", 1520, LocalDate.of(2026, 11, 20), null, null)
        ));
        givenWeeklyDistance("11.25");

        // when
        RunnerSummaryRes result = runnerSummaryService.getRunnerSummaryByUserId(USER_ID);

        // then
        assertThat(result.getRecentRuns())
                .extracting(RunnerRecentRunRes::getDate)
                .containsExactly(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 24), LocalDate.of(2026, 11, 26));
        assertThat(result.getRecentRuns())
                .extracting(RunnerRecentRunRes::getDistanceKm)
                .containsExactly(4.0, 6.25, 5.0);
    }

    @Test
    @DisplayName("심박과 케이던스가 기록된 러닝이 없으면 해당 평균은 null이다")
    void getRunnerSummaryByUserId_withoutHeartRateAndCadence() {
        // given
        givenRecentRecords(List.of(record("5.00", 1500, LocalDate.of(2026, 11, 26), null, null)));
        givenWeeklyDistance("5.00");

        // when
        RunnerSummaryRes result = runnerSummaryService.getRunnerSummaryByUserId(USER_ID);

        // then
        assertThat(result.getAverageHeartRate()).isNull();
        assertThat(result.getAverageCadence()).isNull();
        assertThat(result.getAveragePaceSecPerKm()).isEqualTo(300);
        assertThat(result.getFitnessLevel()).isEqualTo(FitnessLevel.ADVANCED);
    }

    @Test
    @DisplayName("체력 수준은 응답에 담기는 정수 페이스로 판정한다")
    void getRunnerSummaryByUserId_fitnessLevelFollowsDisplayedPace() {
        // given: 329.8초/km → 응답에는 330초/km(5'30")로 나간다
        givenRecentRecords(List.of(record("5.00", 1649, LocalDate.of(2026, 11, 26), null, null)));
        givenWeeklyDistance("5.00");

        // when
        RunnerSummaryRes result = runnerSummaryService.getRunnerSummaryByUserId(USER_ID);

        // then: 5'30"/km로 보이는 러너는 기준(5'30"부터 중급)대로 중급이다
        assertThat(result.getAveragePaceSecPerKm()).isEqualTo(330);
        assertThat(result.getFitnessLevel()).isEqualTo(FitnessLevel.INTERMEDIATE);
    }

    @Test
    @DisplayName("기록이 없으면 기록 수 0, 평균값 null, 빈 목록을 돌려준다")
    void getRunnerSummaryByUserId_withoutRecords() {
        // given
        givenRecentRecords(List.of());
        givenWeeklyDistance("0");

        // when
        RunnerSummaryRes result = runnerSummaryService.getRunnerSummaryByUserId(USER_ID);

        // then
        assertThat(result.getRecordCount()).isZero();
        assertThat(result.getAveragePaceSecPerKm()).isNull();
        assertThat(result.getAverageHeartRate()).isNull();
        assertThat(result.getAverageCadence()).isNull();
        assertThat(result.getAverageDistanceKm()).isNull();
        assertThat(result.getFitnessLevel()).isNull();
        assertThat(result.getWeeklyDistanceKm()).isZero();
        assertThat(result.getRecentRuns()).isEmpty();
    }

    @Test
    @DisplayName("최근 기록은 5개까지, 주간 거리는 오늘을 포함한 최근 7일 범위로 조회한다")
    void getRunnerSummaryByUserId_queryRange() {
        // given
        givenRecentRecords(List.of());
        givenWeeklyDistance("0");

        // when
        runnerSummaryService.getRunnerSummaryByUserId(USER_ID);

        // then
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(runRecordRepository).findRecentWithDistanceAndDuration(eq(USER_ID), pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(5);

        verify(runRecordRepository).sumDistanceBetween(
                USER_ID,
                LocalDateTime.of(2026, 11, 21, 0, 0),
                LocalDateTime.of(2026, 11, 28, 0, 0));
    }

    private void givenRecentRecords(List<RunRecord> records) {
        given(runRecordRepository.findRecentWithDistanceAndDuration(eq(USER_ID), any(Pageable.class)))
                .willReturn(records);
    }

    private void givenWeeklyDistance(String distanceKm) {
        given(runRecordRepository.sumDistanceBetween(eq(USER_ID), any(), any()))
                .willReturn(new BigDecimal(distanceKm));
    }

    private RunRecord record(String distanceKm, int durationSeconds, LocalDate date,
                             Integer averageHeartRate, Integer cadence) {
        return RunRecord.of(null, null, durationSeconds, date.atTime(7, 0),
                new BigDecimal(distanceKm), null, null, null, averageHeartRate, cadence);
    }
}
