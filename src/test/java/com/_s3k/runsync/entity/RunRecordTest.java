package com._s3k.runsync.entity;

import com._s3k.runsync.domain.run.exception.RunRecordErrorCode;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunRecordTest {

    @Test
    @DisplayName("validateOwner - 소유자가 맞으면 예외 없이 통과한다")
    void validateOwner_success() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunRecord record = RunRecord.of(user, null, 1800, LocalDateTime.now(),
                BigDecimal.valueOf(5.0), null, null, null, null, null);
        ReflectionTestUtils.setField(record, "userId", 1L);

        // when & then
        record.validateOwner(1L);
    }

    @Test
    @DisplayName("validateOwner - 다른 유저면 RECORD_NOT_OWNER 예외 발생")
    void validateOwner_notOwner() {
        // given
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunRecord record = RunRecord.of(user, null, 1800, LocalDateTime.now(),
                BigDecimal.valueOf(5.0), null, null, null, null, null);
        ReflectionTestUtils.setField(record, "userId", 1L);

        // when & then
        assertThatThrownBy(() -> record.validateOwner(2L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunRecordErrorCode.RECORD_NOT_OWNER));
    }
}
