package com._s3k.runsync.domain.run.service;

import com._s3k.runsync.domain.run.dto.request.RunSessionStartReq;
import com._s3k.runsync.domain.run.dto.response.RunSessionStartRes;
import com._s3k.runsync.domain.run.exception.RunSessionErrorCode;
import com._s3k.runsync.domain.run.repository.RunningSessionRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import com._s3k.runsync.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RunSessionServiceTest {

    @InjectMocks
    private RunSessionService runSessionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RunningSessionRepository runningSessionRepository;

    @Test
    @DisplayName("러닝 세션 생성 성공")
    void createRunSession_success() {
        // given
        Long userId = 1L;
        RunSessionStartReq request = new RunSessionStartReq();
        request.setStartTime(LocalDateTime.now());

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.existsByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(false);
        given(runningSessionRepository.save(any(RunningSession.class))).willAnswer(i -> i.getArgument(0));

        // when
        RunSessionStartRes result = runSessionService.createRunSession(userId, request);

        // then
        assertThat(result.getStatus()).isEqualTo(RunningSessionStatus.ACTIVE);
        verify(runningSessionRepository).save(any(RunningSession.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저로 러닝 세션 생성 시 예외 발생")
    void createRunSession_userNotFound() {
        // given
        Long userId = 1L;
        RunSessionStartReq request = new RunSessionStartReq();
        request.setStartTime(LocalDateTime.now());

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> runSessionService.createRunSession(userId, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));

        verify(runningSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 진행 중인 세션이 있을 때 예외 발생")
    void createRunSession_activeSessionAlreadyExists() {
        // given
        Long userId = 1L;
        RunSessionStartReq request = new RunSessionStartReq();
        request.setStartTime(LocalDateTime.now());

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.existsByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> runSessionService.createRunSession(userId, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.ACTIVE_SESSION_ALREADY_EXISTS));

        verify(runningSessionRepository, never()).save(any());
    }
}
