package com._s3k.runsync.domain.artrun.service;

import com._s3k.runsync.domain.artrun.dto.request.ArtRunCreateReq;
import com._s3k.runsync.domain.artrun.dto.request.ArtRunStatusUpdateReq;
import com._s3k.runsync.domain.artrun.dto.request.CoordinateReq;
import com._s3k.runsync.domain.artrun.dto.request.MeetingPlaceReq;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunDetailRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunResultParticipantRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunResultRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunScrollRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunStatusRes;
import com._s3k.runsync.domain.artrun.event.ArtRunParticipantLeftEvent;
import com._s3k.runsync.domain.artrun.event.ArtRunSessionClosedEvent;
import com._s3k.runsync.domain.artrun.exception.ArtRunErrorCode;
import com._s3k.runsync.domain.artrun.repository.ArtRunParticipantRepository;
import com._s3k.runsync.domain.artrun.repository.ArtRunSessionRepository;
import com._s3k.runsync.domain.artrun.repository.ParticipantCountProjection;
import com._s3k.runsync.domain.run.repository.RunRecordRepository;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.ArtRunParticipant;
import com._s3k.runsync.entity.ArtRunSession;
import com._s3k.runsync.entity.RunPath;
import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.User;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import com._s3k.runsync.entity.enums.Provider;
import com._s3k.runsync.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArtRunServiceTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @InjectMocks
    private ArtRunService artRunService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtRunSessionRepository artRunSessionRepository;

    @Mock
    private ArtRunParticipantRepository artRunParticipantRepository;

    @Mock
    private RunRecordRepository runRecordRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("협동 러닝 세션 생성 성공 - RECRUITING 상태로 저장되고 좌표가 경도/위도 순으로 저장된다")
    void createArtRun_success() {
        // given
        Long userId = 1L;
        ArtRunCreateReq request = createRequest();

        User host = User.createTmpUser(Provider.KAKAO, "kakaoId", "nickname", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(host));
        given(artRunSessionRepository.save(any(ArtRunSession.class))).willAnswer(i -> i.getArgument(0));

        // when
        artRunService.createArtRun(userId, request);

        // then
        ArgumentCaptor<ArtRunSession> captor = ArgumentCaptor.forClass(ArtRunSession.class);
        verify(artRunSessionRepository).save(captor.capture());
        ArtRunSession saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(ArtRunStatus.RECRUITING);
        assertThat(saved.getMeetingPoint().getX()).isEqualTo(127.1210); // X = longitude
        assertThat(saved.getMeetingPoint().getY()).isEqualTo(37.5202);  // Y = latitude
        assertThat(saved.getRoutePath().getCoordinateN(0).x).isEqualTo(127.1230); // x = longitude
        assertThat(saved.getRoutePath().getCoordinateN(0).y).isEqualTo(37.5210);  // y = latitude
        assertThat(saved.getRoutePath().getNumPoints()).isEqualTo(2);

        ArgumentCaptor<ArtRunParticipant> participantCaptor = ArgumentCaptor.forClass(ArtRunParticipant.class);
        verify(artRunParticipantRepository).save(participantCaptor.capture());
        assertThat(participantCaptor.getValue().getUser()).isEqualTo(host);
    }

    @Test
    @DisplayName("존재하지 않는 유저로 세션 생성 시 예외 발생")
    void createArtRun_userNotFound() {
        // given
        Long userId = 1L;
        ArtRunCreateReq request = createRequest();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> artRunService.createArtRun(userId, request))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));

        verify(artRunSessionRepository, never()).save(any());
        verify(artRunParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("세션 목록 조회 - size+1로 조회해 hasNext/nextCursor와 참가자 수가 매핑된다")
    void getAllArtRuns_success() {
        // given - size=1 이므로 size+1=2개 조회됨 (id DESC)
        ArtRunSession session2 = session(2L, "고구마런");
        ArtRunSession session1 = session(1L, "강아지런");
        ParticipantCountProjection count2 = countProjection(2L, 3L);
        ParticipantCountProjection count1 = countProjection(1L, 5L);
        given(artRunSessionRepository.findByStatusOrderByIdDesc(any(), any(Pageable.class)))
                .willReturn(List.of(session2, session1));
        given(artRunParticipantRepository.countBySessionIds(any()))
                .willReturn(List.of(count2, count1));

        // when
        ArtRunScrollRes result = artRunService.getAllArtRuns(ArtRunStatus.RECRUITING, null, 1);

        // then
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo(2L);          // 보여준 마지막(첫) 항목 id
        assertThat(result.getSessions()).hasSize(1);
        assertThat(result.getSessions().get(0).getSessionId()).isEqualTo(2L);
        assertThat(result.getSessions().get(0).getCurrentCount()).isEqualTo(3); // projection 매핑
    }

    @Test
    @DisplayName("세션 상세 조회 - host/좌표/모임장소/참가자가 응답에 포함된다")
    void getArtRunById_success() {
        // given
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithHost(1L)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.findByArtRunSessionIdWithUser(1L))
                .willReturn(List.of(participant(10L, "현우", session)));

        // when
        ArtRunDetailRes result = artRunService.getArtRunById(1L);

        // then
        assertThat(result.getSessionId()).isEqualTo(1L);
        assertThat(result.getHost().getUserId()).isEqualTo(1L);
        assertThat(result.getCoordinates()).hasSize(2);
        assertThat(result.getCoordinates().get(0).getLatitude()).isEqualTo(37.5210);
        assertThat(result.getCoordinates().get(0).getLongitude()).isEqualTo(127.1230);
        assertThat(result.getMeetingPlace().getName()).isEqualTo("올림픽공원 평화의문 앞");
        assertThat(result.getCurrentCount()).isEqualTo(1);
        assertThat(result.getParticipants()).hasSize(1);
        assertThat(result.getParticipants().get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("존재하지 않는 세션 상세 조회 시 예외 발생")
    void getArtRunById_notFound() {
        // given
        given(artRunSessionRepository.findByIdWithHost(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> artRunService.getArtRunById(1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.SESSION_NOT_FOUND));
    }

    @Test
    @DisplayName("세션 참가 성공 - 모집중이고 정원 여유가 있으면 참가자가 저장된다")
    void joinArtRun_success() {
        // given
        ArtRunSession session = session(1L, "강아지런"); // capacity 5, RECRUITING, host id=1
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(1L, 10L)).willReturn(false);
        given(artRunParticipantRepository.countByArtRunSession_Id(1L)).willReturn(2);
        User user = User.createTmpUser(Provider.KAKAO, "kakao10", "현우", null);
        ReflectionTestUtils.setField(user, "id", 10L);
        given(userRepository.findById(10L)).willReturn(Optional.of(user));

        // when
        artRunService.joinArtRun(10L, 1L);

        // then
        ArgumentCaptor<ArtRunParticipant> captor = ArgumentCaptor.forClass(ArtRunParticipant.class);
        verify(artRunParticipantRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("존재하지 않는 세션 참가 시 예외 발생")
    void joinArtRun_sessionNotFound() {
        // given
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> artRunService.joinArtRun(10L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.SESSION_NOT_FOUND));
        verify(artRunParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("모집중이 아닌 세션 참가 시 예외 발생")
    void joinArtRun_notRecruiting() {
        // given
        ArtRunSession session = session(1L, "강아지런");
        ReflectionTestUtils.setField(session, "status", ArtRunStatus.IN_PROGRESS);
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> artRunService.joinArtRun(10L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.NOT_RECRUITING));
        verify(artRunParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 참가한 세션에 다시 참가 시 예외 발생")
    void joinArtRun_alreadyJoined() {
        // given
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(1L, 10L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> artRunService.joinArtRun(10L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.ALREADY_JOINED));
        verify(artRunParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("정원이 가득 찬 세션 참가 시 예외 발생")
    void joinArtRun_full() {
        // given - capacity 5, 이미 5명
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(1L, 10L)).willReturn(false);
        given(artRunParticipantRepository.countByArtRunSession_Id(1L)).willReturn(5);

        // when & then
        assertThatThrownBy(() -> artRunService.joinArtRun(10L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.SESSION_FULL));
        verify(artRunParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("세션 참가 취소 성공 - 참가자가 삭제된다")
    void leaveArtRun_success() {
        // given - host id=1, 취소 요청자는 10L
        ArtRunSession session = session(1L, "강아지런");
        ArtRunParticipant participant = participant(10L, "현우", session);
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.findByArtRunSession_IdAndUser_Id(1L, 10L)).willReturn(Optional.of(participant));

        // when
        artRunService.leaveArtRun(10L, 1L);

        // then
        verify(artRunParticipantRepository).delete(participant);
        verify(eventPublisher).publishEvent(new ArtRunParticipantLeftEvent(1L, 10L));
    }

    @Test
    @DisplayName("호스트가 참가 취소 시 예외 발생")
    void leaveArtRun_hostCannotLeave() {
        // given - host id=1, 취소 요청자도 1L
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> artRunService.leaveArtRun(1L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.HOST_CANNOT_LEAVE));
        verify(artRunParticipantRepository, never()).delete(any());
    }

    @Test
    @DisplayName("참가하지 않은 세션 취소 시 예외 발생")
    void leaveArtRun_notParticipant() {
        // given - host id=1, 요청자 10L은 미참가
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.findByArtRunSession_IdAndUser_Id(1L, 10L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> artRunService.leaveArtRun(10L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.NOT_PARTICIPANT));
        verify(artRunParticipantRepository, never()).delete(any());
    }

    @Test
    @DisplayName("세션 상태 변경 - 호스트가 모집중 세션을 시작하면 IN_PROGRESS가 된다")
    void updateArtRunStatus_start_success() {
        // given - RECRUITING, host id=1
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when
        ArtRunStatusRes result = artRunService.updateArtRunStatus(1L, 1L, statusReq(ArtRunStatus.IN_PROGRESS));

        // then
        assertThat(result.getStatus()).isEqualTo(ArtRunStatus.IN_PROGRESS);
        assertThat(session.getStatus()).isEqualTo(ArtRunStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("세션 상태 변경 - 호스트가 진행중 세션을 종료하면 COMPLETED가 된다")
    void updateArtRunStatus_complete_success() {
        // given - IN_PROGRESS, host id=1
        ArtRunSession session = session(1L, "강아지런");
        ReflectionTestUtils.setField(session, "status", ArtRunStatus.IN_PROGRESS);
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when
        ArtRunStatusRes result = artRunService.updateArtRunStatus(1L, 1L, statusReq(ArtRunStatus.COMPLETED));

        // then
        assertThat(result.getStatus()).isEqualTo(ArtRunStatus.COMPLETED);
    }

    @Test
    @DisplayName("호스트가 아닌 유저가 상태 변경 시 예외 발생")
    void updateArtRunStatus_notHost() {
        // given - host id=1, 요청자 99L
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> artRunService.updateArtRunStatus(99L, 1L, statusReq(ArtRunStatus.IN_PROGRESS)))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.NOT_HOST));
    }

    @Test
    @DisplayName("잘못된 상태 전이(모집중→완료) 시 예외 발생")
    void updateArtRunStatus_invalidTransition() {
        // given - RECRUITING 상태에서 곧바로 COMPLETED 요청
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> artRunService.updateArtRunStatus(1L, 1L, statusReq(ArtRunStatus.COMPLETED)))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.INVALID_STATUS_TRANSITION));
    }

    @Test
    @DisplayName("존재하지 않는 세션 상태 변경 시 예외 발생")
    void updateArtRunStatus_sessionNotFound() {
        // given
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> artRunService.updateArtRunStatus(1L, 1L, statusReq(ArtRunStatus.IN_PROGRESS)))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.SESSION_NOT_FOUND));
    }

    @Test
    @DisplayName("세션 삭제 성공 - 호스트가 모집중 세션을 삭제하면 참가자도 함께 삭제된다")
    void deleteArtRun_success() {
        // given - RECRUITING, host id=1
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when
        artRunService.deleteArtRun(1L, 1L);

        // then
        verify(artRunParticipantRepository).deleteByArtRunSession_Id(1L);
        verify(artRunSessionRepository).delete(session);
        verify(eventPublisher).publishEvent(new ArtRunSessionClosedEvent(1L));
    }

    @Test
    @DisplayName("호스트가 아닌 유저가 세션 삭제 시 예외 발생")
    void deleteArtRun_notHost() {
        // given - host id=1, 요청자 99L
        ArtRunSession session = session(1L, "강아지런");
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> artRunService.deleteArtRun(99L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.NOT_HOST));
        verify(artRunSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("모집중이 아닌 세션 삭제 시 예외 발생")
    void deleteArtRun_notRecruiting() {
        // given - IN_PROGRESS
        ArtRunSession session = session(1L, "강아지런");
        ReflectionTestUtils.setField(session, "status", ArtRunStatus.IN_PROGRESS);
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> artRunService.deleteArtRun(1L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.NOT_RECRUITING));
        verify(artRunParticipantRepository, never()).deleteByArtRunSession_Id(any());
        verify(artRunSessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("존재하지 않는 세션 삭제 시 예외 발생")
    void deleteArtRun_sessionNotFound() {
        // given
        given(artRunSessionRepository.findByIdWithLock(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> artRunService.deleteArtRun(1L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.SESSION_NOT_FOUND));
    }

    @Test
    @DisplayName("협동 러닝 결과 조회 성공 - 뛴 참가자는 기록, 안 뛴 참가자는 빈 경로(paths=[])로 포함된다")
    void getArtRunResult_success() {
        // given
        Long sessionId = 100L;
        Long runnerId = 10L;
        Long nonRunnerId = 11L;
        ArtRunSession session = session(sessionId, "강아지런");
        ReflectionTestUtils.setField(session, "status", ArtRunStatus.COMPLETED);

        given(artRunSessionRepository.findByIdWithHost(sessionId)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(sessionId, runnerId)).willReturn(true);
        given(artRunParticipantRepository.findByArtRunSessionIdWithUser(sessionId))
                .willReturn(List.of(participant(runnerId, "러너", session), participant(nonRunnerId, "구경꾼", session)));
        given(runRecordRepository.findByArtRunSessionIdWithPaths(sessionId))
                .willReturn(List.of(runRecord(runnerId, 2.59, 1048)));

        // when
        ArtRunResultRes result = artRunService.getArtRunResultBySessionId(runnerId, sessionId);

        // then
        assertThat(result.getSessionId()).isEqualTo(sessionId);
        assertThat(result.getStatus()).isEqualTo(ArtRunStatus.COMPLETED);
        assertThat(result.getDesignCoordinates()).hasSize(2);
        assertThat(result.getParticipants()).hasSize(2);

        ArtRunResultParticipantRes runner = result.getParticipants().stream()
                .filter(p -> p.getUserId().equals(runnerId)).findFirst().orElseThrow();
        assertThat(runner.getDistance()).isEqualTo(2.59);
        assertThat(runner.getDurationSeconds()).isEqualTo(1048);
        assertThat(runner.getPaths()).hasSize(1);

        ArtRunResultParticipantRes nonRunner = result.getParticipants().stream()
                .filter(p -> p.getUserId().equals(nonRunnerId)).findFirst().orElseThrow();
        assertThat(nonRunner.getDistance()).isEqualTo(0.0);
        assertThat(nonRunner.getDurationSeconds()).isEqualTo(0);
        assertThat(nonRunner.getPaths()).isEmpty();
    }

    @Test
    @DisplayName("호스트는 참가자가 아니어도 결과를 조회할 수 있다")
    void getArtRunResult_hostCanView() {
        // given
        Long sessionId = 100L;
        ArtRunSession session = session(sessionId, "강아지런"); // host id == 100
        ReflectionTestUtils.setField(session, "status", ArtRunStatus.COMPLETED);
        given(artRunSessionRepository.findByIdWithHost(sessionId)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.findByArtRunSessionIdWithUser(sessionId)).willReturn(List.of());
        given(runRecordRepository.findByArtRunSessionIdWithPaths(sessionId)).willReturn(List.of());

        // when
        ArtRunResultRes result = artRunService.getArtRunResultBySessionId(100L, sessionId);

        // then
        assertThat(result.getParticipants()).isEmpty();
    }

    @Test
    @DisplayName("종료되지 않은(IN_PROGRESS) 세션 결과 조회 시 예외 발생")
    void getArtRunResult_notCompleted() {
        // given
        Long sessionId = 100L;
        ArtRunSession session = session(sessionId, "강아지런"); // host id == 100
        ReflectionTestUtils.setField(session, "status", ArtRunStatus.IN_PROGRESS);
        given(artRunSessionRepository.findByIdWithHost(sessionId)).willReturn(Optional.of(session));

        // when & then (호스트라 권한은 통과, 상태 검증에서 막힘)
        assertThatThrownBy(() -> artRunService.getArtRunResultBySessionId(100L, sessionId))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.ARTRUN_NOT_COMPLETED));
    }

    @Test
    @DisplayName("참가자도 호스트도 아니면 결과 조회 시 예외 발생")
    void getArtRunResult_accessDenied() {
        // given
        Long sessionId = 100L;
        Long strangerId = 99L;
        ArtRunSession session = session(sessionId, "강아지런"); // host id == 100
        given(artRunSessionRepository.findByIdWithHost(sessionId)).willReturn(Optional.of(session));
        given(artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(sessionId, strangerId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> artRunService.getArtRunResultBySessionId(strangerId, sessionId))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.RESULT_ACCESS_DENIED));
    }

    @Test
    @DisplayName("존재하지 않는 세션 결과 조회 시 예외 발생")
    void getArtRunResult_sessionNotFound() {
        // given
        given(artRunSessionRepository.findByIdWithHost(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> artRunService.getArtRunResultBySessionId(1L, 1L))
                .isInstanceOf(GlobalException.class)
                .satisfies(e -> assertThat(((GlobalException) e).getResultCode())
                        .isEqualTo(ArtRunErrorCode.SESSION_NOT_FOUND));
    }

    private ArtRunStatusUpdateReq statusReq(ArtRunStatus status) {
        ArtRunStatusUpdateReq request = new ArtRunStatusUpdateReq();
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }

    private ArtRunSession session(Long id, String title) {
        User host = User.createTmpUser(Provider.KAKAO, "kakao" + id, "host" + id, null);
        ReflectionTestUtils.setField(host, "id", id);

        Point meetingPoint = GEOMETRY_FACTORY.createPoint(new Coordinate(127.1210, 37.5202));
        LineString routePath = GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                new Coordinate(127.1230, 37.5210),
                new Coordinate(127.1242, 37.5215)
        });
        ArtRunSession session = ArtRunSession.of(host, title, 5, LocalDateTime.now().plusDays(1),
                "올림픽공원 평화의문 앞", meetingPoint, routePath);
        ReflectionTestUtils.setField(session, "id", id);
        return session;
    }

    private ArtRunParticipant participant(Long userId, String nickname, ArtRunSession session) {
        User user = User.createTmpUser(Provider.KAKAO, "kakao" + userId, nickname, null);
        ReflectionTestUtils.setField(user, "id", userId);
        return ArtRunParticipant.of(session, user);
    }

    private RunRecord runRecord(Long userId, double distance, int durationSeconds) {
        User user = User.createTmpUser(Provider.KAKAO, "kakao" + userId, "nick" + userId, null);
        ReflectionTestUtils.setField(user, "id", userId);
        RunRecord record = RunRecord.of(user, null, durationSeconds, LocalDateTime.now(),
                BigDecimal.valueOf(distance), null, null, null, null, null);
        ReflectionTestUtils.setField(record, "userId", userId);
        record.addPaths(List.of(RunPath.of(record, 37.5, 127.0, 1, LocalDateTime.now(), null, null)));
        return record;
    }

    private ParticipantCountProjection countProjection(Long sessionId, Long count) {
        ParticipantCountProjection projection = mock(ParticipantCountProjection.class);
        given(projection.getSessionId()).willReturn(sessionId);
        given(projection.getParticipantCount()).willReturn(count);
        return projection;
    }

    private ArtRunCreateReq createRequest() {
        MeetingPlaceReq place = new MeetingPlaceReq();
        ReflectionTestUtils.setField(place, "name", "올림픽공원 평화의문 앞");
        ReflectionTestUtils.setField(place, "latitude", 37.5202);
        ReflectionTestUtils.setField(place, "longitude", 127.1210);

        ArtRunCreateReq request = new ArtRunCreateReq();
        ReflectionTestUtils.setField(request, "title", "강아지런 같이 그려요");
        ReflectionTestUtils.setField(request, "capacity", 5);
        ReflectionTestUtils.setField(request, "meetingTime", LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(request, "meetingPlace", place);
        ReflectionTestUtils.setField(request, "coordinates", List.of(coord(37.5210, 127.1230), coord(37.5215, 127.1242)));
        return request;
    }

    private CoordinateReq coord(double latitude, double longitude) {
        CoordinateReq coordinate = new CoordinateReq();
        ReflectionTestUtils.setField(coordinate, "latitude", latitude);
        ReflectionTestUtils.setField(coordinate, "longitude", longitude);
        return coordinate;
    }
}
