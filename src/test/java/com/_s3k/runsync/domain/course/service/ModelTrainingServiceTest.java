package com._s3k.runsync.domain.course.service;

import com._s3k.runsync.domain.course.api.ModelTrainingClient;
import com._s3k.runsync.domain.course.dto.request.AiTrainingReq;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.Gender;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ModelTrainingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 28);

    @Mock
    private RunRecordRepository runRecordRepository;

    @Mock
    private ModelTrainingClient modelTrainingClient;

    // 한국 시간 2026-11-28 04:00
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-27T19:00:00Z"), ZoneId.of("Asia/Seoul"));

    private ModelTrainingService modelTrainingService;

    @BeforeEach
    void setUp() {
        modelTrainingService = new ModelTrainingService(runRecordRepository, modelTrainingClient, clock);
    }

    @Test
    @DisplayName("러닝 기록과 사용자 정보를 재학습 요청으로 바꿔 추천 서버에 보낸다")
    void createTraining_sendsRecords() {
        // given
        given(modelTrainingClient.isEnabled()).willReturn(true);
        User user = user(1L, 25, Gender.MALE);
        RunRecord record = RunRecord.of(user, null, 1900, LocalDateTime.of(2026, 11, 20, 7, 0),
                new BigDecimal("5.00"), null, null, new BigDecimal("22.00"), 156, 164);
        RunRecord withoutDetail = RunRecord.of(user(2L, null, null), null, 1380, LocalDateTime.of(2026, 11, 21, 21, 0),
                new BigDecimal("3.00"), null, null, null, null, null);
        given(runRecordRepository.findRecentForTraining(any(Pageable.class))).willReturn(List.of(record, withoutDetail));

        // when
        modelTrainingService.createTraining();

        // then
        ArgumentCaptor<AiTrainingReq> captor = ArgumentCaptor.forClass(AiTrainingReq.class);
        verify(modelTrainingClient).train(captor.capture());
        List<AiTrainingReq.TrainingRecord> sent = captor.getValue().getRecords();
        assertThat(sent).hasSize(2);

        AiTrainingReq.TrainingRecord first = sent.get(0);
        assertThat(first.getUserId()).isEqualTo(1L);
        assertThat(first.getAge()).isEqualTo(25);
        assertThat(first.getGender()).isEqualTo("MALE");
        assertThat(first.getStartTime()).isEqualTo("2026-11-20T07:00");
        assertThat(first.getDistanceKm()).isEqualTo(5.0);
        assertThat(first.getDurationSec()).isEqualTo(1900);
        assertThat(first.getAvgHeartRate()).isEqualTo(156);
        assertThat(first.getCadence()).isEqualTo(164);
        assertThat(first.getElevationGainM()).isEqualTo(22.0);

        // 심박·나이가 없는 기록도 그대로 보낸다. 학습에 쓸지는 추천 서버가 판단한다
        AiTrainingReq.TrainingRecord second = sent.get(1);
        assertThat(second.getAge()).isNull();
        assertThat(second.getGender()).isNull();
        assertThat(second.getAvgHeartRate()).isNull();
        assertThat(second.getElevationGainM()).isNull();
    }

    @Test
    @DisplayName("한 번에 보내는 기록 수를 상한까지로 제한해 조회한다")
    void createTraining_limitsRecords() {
        // given
        given(modelTrainingClient.isEnabled()).willReturn(true);
        given(runRecordRepository.findRecentForTraining(any(Pageable.class))).willReturn(List.of());

        // when
        modelTrainingService.createTraining();

        // then
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(runRecordRepository).findRecentForTraining(pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(ModelTrainingService.MAX_TRAINING_RECORDS);
    }

    @Test
    @DisplayName("내부 호출 키가 설정되지 않았으면 기록을 조회하지도 호출하지도 않는다")
    void createTraining_skippedWithoutKey() {
        // given
        given(modelTrainingClient.isEnabled()).willReturn(false);

        // when
        modelTrainingService.createTraining();

        // then
        verify(runRecordRepository, never()).findRecentForTraining(any());
        verify(modelTrainingClient, never()).train(any());
    }

    @Test
    @DisplayName("보낼 기록이 없으면 추천 서버를 호출하지 않는다")
    void createTraining_skippedWithoutRecords() {
        // given
        given(modelTrainingClient.isEnabled()).willReturn(true);
        given(runRecordRepository.findRecentForTraining(any(Pageable.class))).willReturn(List.of());

        // when
        modelTrainingService.createTraining();

        // then
        verify(modelTrainingClient, never()).train(any());
    }

    @Test
    @DisplayName("스케줄 실행 중 추천 서버 호출이 실패해도 예외가 밖으로 나가지 않는다")
    void runScheduledTraining_swallowsFailure() {
        // given
        given(modelTrainingClient.isEnabled()).willReturn(true);
        User user = user(1L, 25, Gender.MALE);
        RunRecord record = RunRecord.of(user, null, 1900, LocalDateTime.of(2026, 11, 20, 7, 0),
                new BigDecimal("5.00"), null, null, null, 156, null);
        given(runRecordRepository.findRecentForTraining(any(Pageable.class))).willReturn(List.of(record));
        given(modelTrainingClient.train(any())).willThrow(new IllegalStateException("connection refused"));

        // when & then
        assertThatCode(() -> modelTrainingService.runScheduledTraining()).doesNotThrowAnyException();
    }

    // 나이 계산 자체는 UserTest 에서 검증한다. 여기서는 오늘 날짜로 물어보는지만 확인한다
    private User user(Long id, Integer age, Gender gender) {
        User user = mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.calculateAge(TODAY)).willReturn(age);
        given(user.getGender()).willReturn(gender);
        return user;
    }
}
