package com._s3k.runsync.domain.run.service;

import com._s3k.runsync.domain.run.exception.RunRecordErrorCode;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RunRecordServiceTest {

    @InjectMocks
    private RunRecordService runRecordService;

    @Mock
    private RunRecordRepository runRecordRepository;

    @Test
    @DisplayName("러닝 기록 상세 조회 성공")
    void getRunRecordById_success() {
        // given
        Long userId = 1L;
        Long recordId = 1L;

        RunRecord record = mock(RunRecord.class);
        given(record.getId()).willReturn(recordId);
        given(record.getStartTime()).willReturn(LocalDateTime.now());
        given(record.getDurationSeconds()).willReturn(1800);
        given(record.getDistance()).willReturn(BigDecimal.valueOf(5.0));
        given(record.getPaths()).willReturn(List.of());
        given(runRecordRepository.findByIdWithPaths(recordId)).willReturn(Optional.of(record));

        // when
        var result = runRecordService.getRunRecordById(userId, recordId);

        // then
        assertThat(result.getRecordId()).isEqualTo(recordId);
        assertThat(result.getPaths()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 기록 조회 시 예외 발생")
    void getRunRecordById_recordNotFound() {
        // given
        given(runRecordRepository.findByIdWithPaths(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> runRecordService.getRunRecordById(1L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunRecordErrorCode.RECORD_NOT_FOUND));
    }

    @Test
    @DisplayName("다른 유저의 기록 조회 시 예외 발생")
    void getRunRecordById_notOwner() {
        // given
        RunRecord record = mock(RunRecord.class);
        given(runRecordRepository.findByIdWithPaths(1L)).willReturn(Optional.of(record));
        doThrow(new GlobalException(RunRecordErrorCode.RECORD_NOT_OWNER)).when(record).validateOwner(1L);

        // when & then
        assertThatThrownBy(() -> runRecordService.getRunRecordById(1L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunRecordErrorCode.RECORD_NOT_OWNER));
    }
}
