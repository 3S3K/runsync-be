package com._s3k.runsync.domain.artrun.service;

import com._s3k.runsync.domain.artrun.dto.request.ArtRunCreateReq;
import com._s3k.runsync.domain.artrun.dto.request.CoordinateReq;
import com._s3k.runsync.domain.artrun.dto.request.MeetingPlaceReq;
import com._s3k.runsync.domain.artrun.repository.ArtRunParticipantRepository;
import com._s3k.runsync.domain.artrun.repository.ArtRunSessionRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArtRunServiceTest {

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
