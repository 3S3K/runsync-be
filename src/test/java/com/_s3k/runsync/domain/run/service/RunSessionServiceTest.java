package com._s3k.runsync.domain.run.service;

import com._s3k.runsync.domain.run.dto.request.LocationUpdateReq;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());

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
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());

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
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());

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

    @Test
    @DisplayName("존재하지 않는 세션으로 위치 저장 시 예외 발생")
    void updateLocation_sessionNotFound() {
        // given
        given(runningSessionRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> runSessionService.updateLocation(1L, 1L, new LocationUpdateReq()))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_FOUND));
    }

    @Test
    @DisplayName("다른 유저의 세션에 위치 저장 시 예외 발생")
    void updateLocation_sessionNotOwner() {
        // given
        RunningSession session = mock(RunningSession.class);
        given(session.getUserId()).willReturn(2L);
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> runSessionService.updateLocation(1L, 1L, new LocationUpdateReq()))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_OWNER));
    }

    @Test
    @DisplayName("ACTIVE 상태가 아닌 세션에 위치 저장 시 예외 발생")
    void updateLocation_sessionNotActive() {
        // given
        RunningSession session = mock(RunningSession.class);
        given(session.getUserId()).willReturn(1L);
        given(session.getStatus()).willReturn(RunningSessionStatus.COMPLETED);
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> runSessionService.updateLocation(1L, 1L, new LocationUpdateReq()))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_ACTIVE));
    }
}
