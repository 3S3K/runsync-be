package com._s3k.runsync.domain.run.service;

import com._s3k.runsync.domain.artrun.exception.ArtRunErrorCode;
import com._s3k.runsync.domain.artrun.repository.ArtRunParticipantRepository;
import com._s3k.runsync.domain.artrun.repository.ArtRunSessionRepository;
import com._s3k.runsync.domain.location.service.LocationService;
import com._s3k.runsync.domain.run.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.run.dto.request.RunRecordDetailReq;
import com._s3k.runsync.domain.run.dto.request.RunSessionEndReq;
import com._s3k.runsync.domain.run.dto.request.RunSessionStartReq;
import com._s3k.runsync.domain.run.dto.response.RunSessionActiveRes;
import com._s3k.runsync.domain.run.exception.RunSessionErrorCode;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.run.repository.RunSessionRedisRepository;
import com._s3k.runsync.domain.run.repository.RunningSessionRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.RunningSession;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import com._s3k.runsync.global.exception.GlobalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    private RunRecordRepository runRecordRepository;

    @Mock
    private RunSessionRedisRepository runSessionRedisRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private ArtRunParticipantRepository artRunParticipantRepository;

    @Mock
    private ArtRunSessionRepository artRunSessionRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-10T00:00:00Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("러닝 세션 생성 성공")
    void createRunSession_success() {
        // given
        Long userId = 1L;
        RunSessionStartReq request = new RunSessionStartReq();
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(Optional.empty());
        given(runningSessionRepository.save(any(RunningSession.class))).willAnswer(i -> i.getArgument(0));

        // when
        runSessionService.createRunSession(userId, request);

        // then
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
    @DisplayName("최근 진행 중인 세션이 있으면 예외 발생")
    void createRunSession_activeSessionAlreadyExists() {
        // given
        Long userId = 1L;
        RunSessionStartReq request = new RunSessionStartReq();
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession active = mock(RunningSession.class);
        given(active.isStale(any())).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(Optional.of(active));

        // when & then
        assertThatThrownBy(() -> runSessionService.createRunSession(userId, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.ACTIVE_SESSION_ALREADY_EXISTS));

        verify(runningSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("오래 방치된 ACTIVE 세션은 자동 정리되고 새 세션이 시작된다")
    void createRunSession_staleSessionAutoAbandoned() {
        // given
        Long userId = 1L;
        RunSessionStartReq request = new RunSessionStartReq();
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession stale = mock(RunningSession.class);
        given(stale.isStale(any())).willReturn(true);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(Optional.of(stale));
        given(runningSessionRepository.save(any(RunningSession.class))).willAnswer(i -> i.getArgument(0));

        // when
        runSessionService.createRunSession(userId, request);

        // then
        verify(stale).abandon();
        verify(runningSessionRepository).saveAndFlush(stale);
        verify(runningSessionRepository).save(any(RunningSession.class));
    }

    @Test
    @DisplayName("진행 중 세션 조회 - 있으면 세션 정보를 반환한다")
    void getActiveRunSession_exists() {
        // given
        Long userId = 1L;
        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = RunningSession.of(user, LocalDateTime.now(), 100L);
        ReflectionTestUtils.setField(session, "id", 5L);
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE))
                .willReturn(Optional.of(session));

        // when
        RunSessionActiveRes result = runSessionService.getActiveRunSession(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(5L);
        assertThat(result.getArtRunSessionId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(RunningSessionStatus.ACTIVE);
    }

    @Test
    @DisplayName("진행 중 세션 조회 - 없으면 null을 반환한다")
    void getActiveRunSession_none() {
        // given
        Long userId = 1L;
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when
        RunSessionActiveRes result = runSessionService.getActiveRunSession(userId);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("협동 러닝 참가자가 artRunSessionId를 보내면 링크가 저장된다")
    void createRunSession_withArtRunSessionId_success() {
        // given
        Long userId = 1L;
        Long artRunSessionId = 100L;
        RunSessionStartReq request = new RunSessionStartReq();
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "artRunSessionId", artRunSessionId);

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        ArtRunSession artRunSession = mock(ArtRunSession.class);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(Optional.empty());
        given(artRunSessionRepository.findById(artRunSessionId)).willReturn(Optional.of(artRunSession));
        given(artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(artRunSessionId, userId)).willReturn(true);
        given(runningSessionRepository.save(any(RunningSession.class))).willAnswer(i -> i.getArgument(0));

        // when
        runSessionService.createRunSession(userId, request);

        // then
        ArgumentCaptor<RunningSession> captor = ArgumentCaptor.forClass(RunningSession.class);
        verify(runningSessionRepository).save(captor.capture());
        assertThat(captor.getValue().getArtRunSessionId()).isEqualTo(artRunSessionId);
        verify(artRunSession).validateInProgress();
    }

    @Test
    @DisplayName("협동 러닝 비참가자가 artRunSessionId를 보내면 예외 발생")
    void createRunSession_artRunNotParticipant() {
        // given
        Long userId = 1L;
        Long artRunSessionId = 100L;
        RunSessionStartReq request = new RunSessionStartReq();
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "artRunSessionId", artRunSessionId);

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(Optional.empty());
        given(artRunSessionRepository.findById(artRunSessionId)).willReturn(Optional.of(mock(ArtRunSession.class)));
        given(artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(artRunSessionId, userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> runSessionService.createRunSession(userId, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.NOT_PARTICIPANT));

        verify(runningSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 협동 러닝 세션 ID를 보내면 예외 발생")
    void createRunSession_artRunSessionNotFound() {
        // given
        Long userId = 1L;
        Long artRunSessionId = 100L;
        RunSessionStartReq request = new RunSessionStartReq();
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "artRunSessionId", artRunSessionId);

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(Optional.empty());
        given(artRunSessionRepository.findById(artRunSessionId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> runSessionService.createRunSession(userId, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.SESSION_NOT_FOUND));

        verify(runningSessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("진행 중이 아닌 협동 러닝에 링크 시도 시 예외 발생")
    void createRunSession_artRunNotInProgress() {
        // given
        Long userId = 1L;
        Long artRunSessionId = 100L;
        RunSessionStartReq request = new RunSessionStartReq();
        ReflectionTestUtils.setField(request, "startTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "artRunSessionId", artRunSessionId);

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        ArtRunSession artRunSession = mock(ArtRunSession.class);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(runningSessionRepository.findByUserIdAndStatus(userId, RunningSessionStatus.ACTIVE)).willReturn(Optional.empty());
        given(artRunSessionRepository.findById(artRunSessionId)).willReturn(Optional.of(artRunSession));
        given(artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(artRunSessionId, userId)).willReturn(true);
        doThrow(new GlobalException(ArtRunErrorCode.ARTRUN_NOT_IN_PROGRESS)).when(artRunSession).validateInProgress();

        // when & then
        assertThatThrownBy(() -> runSessionService.createRunSession(userId, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.ARTRUN_NOT_IN_PROGRESS));

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
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));
        doThrow(new GlobalException(RunSessionErrorCode.SESSION_NOT_OWNER)).when(session).validateOwner(1L);

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
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));
        doThrow(new GlobalException(RunSessionErrorCode.SESSION_NOT_ACTIVE)).when(session).validateActive();

        // when & then
        assertThatThrownBy(() -> runSessionService.updateLocation(1L, 1L, new LocationUpdateReq()))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_ACTIVE));
    }

    @Test
    @DisplayName("경로가 있는 러닝 종료 성공")
    void endRunSession_success_withPaths() {
        // given
        Long userId = 1L;
        Long sessionId = 1L;
        RunSessionEndReq request = new RunSessionEndReq();
        ReflectionTestUtils.setField(request, "endTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "totalDistance", 5.0);

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = mock(RunningSession.class);
        given(session.createRecord(any())).willReturn(
                RunRecord.of(user, session, 0, LocalDateTime.now(), BigDecimal.valueOf(5.0), null, null, null, null, null)
        );
        given(runningSessionRepository.findById(sessionId)).willReturn(Optional.of(session));

        String pathJson = "{\"lat\":37.5665,\"lng\":126.9780,\"speed\":3.5,\"recordedAt\":\"2026-05-13T02:43:10Z\"}";
        given(runSessionRedisRepository.getPaths(sessionId)).willReturn(List.of(pathJson));

        // when
        runSessionService.endRunSession(userId, sessionId, request);

        // then
        verify(runRecordRepository).save(any(RunRecord.class));
        verify(runSessionRedisRepository).deletePaths(sessionId);
        verify(runSessionRedisRepository).deleteState(sessionId);
        verify(locationService).removeLocation(userId);
    }

    @Test
    @DisplayName("경로가 없는 러닝 종료 성공")
    void endRunSession_success_withNoPaths() {
        // given
        Long userId = 1L;
        Long sessionId = 1L;
        RunSessionEndReq request = new RunSessionEndReq();
        ReflectionTestUtils.setField(request, "endTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "totalDistance", 5.0);

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = mock(RunningSession.class);
        given(session.createRecord(any())).willReturn(
                RunRecord.of(user, session, 0, LocalDateTime.now(), BigDecimal.valueOf(5.0), null, null, null, null, null)
        );
        given(runningSessionRepository.findById(sessionId)).willReturn(Optional.of(session));
        given(runSessionRedisRepository.getPaths(sessionId)).willReturn(List.of());

        // when
        runSessionService.endRunSession(userId, sessionId, request);

        // then
        verify(runRecordRepository).save(any(RunRecord.class));
        verify(runSessionRedisRepository).deletePaths(sessionId);
        verify(runSessionRedisRepository).deleteState(sessionId);
        verify(locationService).removeLocation(userId);
    }

    @Test
    @DisplayName("존재하지 않는 세션으로 러닝 종료 시 예외 발생")
    void endRunSession_sessionNotFound() {
        // given
        RunSessionEndReq request = new RunSessionEndReq();
        ReflectionTestUtils.setField(request, "endTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "totalDistance", 5.0);
        given(runningSessionRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> runSessionService.endRunSession(1L, 1L, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_FOUND));

        verify(runRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("다른 유저의 세션 종료 시 예외 발생")
    void endRunSession_sessionNotOwner() {
        // given
        RunSessionEndReq request = new RunSessionEndReq();
        ReflectionTestUtils.setField(request, "endTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "totalDistance", 5.0);

        RunningSession session = mock(RunningSession.class);
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));
        doThrow(new GlobalException(RunSessionErrorCode.SESSION_NOT_OWNER)).when(session).validateOwner(1L);

        // when & then
        assertThatThrownBy(() -> runSessionService.endRunSession(1L, 1L, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_OWNER));

        verify(runRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("ACTIVE 상태가 아닌 세션 종료 시 예외 발생")
    void endRunSession_sessionNotActive() {
        // given
        RunSessionEndReq request = new RunSessionEndReq();
        ReflectionTestUtils.setField(request, "endTime", LocalDateTime.now());
        ReflectionTestUtils.setField(request, "totalDistance", 5.0);

        RunningSession session = mock(RunningSession.class);
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));
        doThrow(new GlobalException(RunSessionErrorCode.SESSION_NOT_ACTIVE)).when(session).validateActive();

        // when & then
        assertThatThrownBy(() -> runSessionService.endRunSession(1L, 1L, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_ACTIVE));

        verify(runRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("러닝 기록 세부 입력 저장 성공")
    void saveRunRecordDetail_success() {
        // given
        Long userId = 1L;
        Long sessionId = 1L;
        RunRecordDetailReq request = new RunRecordDetailReq();
        ReflectionTestUtils.setField(request, "averagePace", 6.43);
        ReflectionTestUtils.setField(request, "calories", 450);

        User user = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        RunningSession session = mock(RunningSession.class);
        RunRecord record = RunRecord.of(user, session, 1800, LocalDateTime.now(), BigDecimal.valueOf(5.0), null, null, null, null, null);

        given(runningSessionRepository.findById(sessionId)).willReturn(Optional.of(session));
        given(runRecordRepository.findByRunningSessionId(sessionId)).willReturn(Optional.of(record));

        // when
        runSessionService.saveRunRecordDetail(userId, sessionId, request);

        // then
        verify(session).validateOwner(userId);
        verify(session).validateCompleted();
    }

    @Test
    @DisplayName("존재하지 않는 세션으로 기록 세부 입력 시 예외 발생")
    void saveRunRecordDetail_sessionNotFound() {
        // given
        given(runningSessionRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> runSessionService.saveRunRecordDetail(1L, 1L, new RunRecordDetailReq()))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_FOUND));
    }

    @Test
    @DisplayName("다른 유저의 세션에 기록 세부 입력 시 예외 발생")
    void saveRunRecordDetail_sessionNotOwner() {
        // given
        RunningSession session = mock(RunningSession.class);
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));
        doThrow(new GlobalException(RunSessionErrorCode.SESSION_NOT_OWNER)).when(session).validateOwner(1L);

        // when & then
        assertThatThrownBy(() -> runSessionService.saveRunRecordDetail(1L, 1L, new RunRecordDetailReq()))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_OWNER));
    }

    @Test
    @DisplayName("완료되지 않은 세션에 기록 세부 입력 시 예외 발생")
    void saveRunRecordDetail_sessionNotCompleted() {
        // given
        RunningSession session = mock(RunningSession.class);
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));
        doThrow(new GlobalException(RunSessionErrorCode.SESSION_NOT_COMPLETED)).when(session).validateCompleted();

        // when & then
        assertThatThrownBy(() -> runSessionService.saveRunRecordDetail(1L, 1L, new RunRecordDetailReq()))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.SESSION_NOT_COMPLETED));
    }

    @Test
    @DisplayName("런 레코드가 없는 세션에 기록 세부 입력 시 예외 발생")
    void saveRunRecordDetail_runRecordNotFound() {
        // given
        RunningSession session = mock(RunningSession.class);
        given(runningSessionRepository.findById(1L)).willReturn(Optional.of(session));
        given(runRecordRepository.findByRunningSessionId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> runSessionService.saveRunRecordDetail(1L, 1L, new RunRecordDetailReq()))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(RunSessionErrorCode.RUN_RECORD_NOT_FOUND));
    }
}
