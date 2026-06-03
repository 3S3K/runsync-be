package com._s3k.runsync.domain.artrun.service;

import com._s3k.runsync.domain.artrun.dto.request.ArtRunCreateReq;
import com._s3k.runsync.domain.artrun.dto.request.CoordinateReq;
import com._s3k.runsync.domain.artrun.dto.request.MeetingPlaceReq;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunDetailRes;
import com._s3k.runsync.domain.artrun.dto.response.ArtRunScrollRes;
import com._s3k.runsync.domain.artrun.exception.ArtRunErrorCode;
import com._s3k.runsync.domain.artrun.repository.ArtRunParticipantRepository;
import com._s3k.runsync.domain.artrun.repository.ArtRunSessionRepository;
import com._s3k.runsync.domain.artrun.repository.ParticipantCountProjection;
import com._s3k.runsync.domain.users.exception.UserErrorCode;
import com._s3k.runsync.domain.users.repository.UserRepository;
import com._s3k.runsync.entity.ArtRunParticipant;
import com._s3k.runsync.entity.ArtRunSession;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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
