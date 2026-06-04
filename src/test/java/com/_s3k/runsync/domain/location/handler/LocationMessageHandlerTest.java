package com._s3k.runsync.domain.location.handler;

import com._s3k.runsync.domain.artrun.service.ArtRunService;
import com._s3k.runsync.domain.location.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.location.service.LocationService;
import com._s3k.runsync.domain.run.service.RunSessionService;
import com._s3k.runsync.global.websocket.dto.WebSocketMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LocationMessageHandlerTest {

    @InjectMocks
    private LocationMessageHandler handler;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private LocationService locationService;

    @Mock
    private RunSessionService runSessionService;

    @Mock
    private ArtRunService artRunService;

    private final Principal principal = () -> "10";

    @Test
    @DisplayName("협동 러닝 참가자가 위치를 보내면 /topic/artrun/{id}로도 발행된다")
    void handleLocation_artRunParticipant_publishesArtRunTopic() {
        // given - artRunSessionId 100, 발행자(10)는 참가자
        given(artRunService.isParticipant(10L, 100L)).willReturn(true);
        WebSocketMessage<LocationUpdateReq> message = locationMessage(100L);

        // when
        handler.handleLocation(message, principal);

        // then - 친구 토픽 + 협동러닝 토픽 둘 다 발행
        verify(messagingTemplate).convertAndSend(eq("/topic/location/10"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/artrun/100"), any(Object.class));
    }

    @Test
    @DisplayName("참가자가 아니면 /topic/artrun/{id}로 발행되지 않는다")
    void handleLocation_notParticipant_doesNotPublishArtRunTopic() {
        // given - artRunSessionId 100, 발행자(10)는 비참가자
        given(artRunService.isParticipant(10L, 100L)).willReturn(false);
        WebSocketMessage<LocationUpdateReq> message = locationMessage(100L);

        // when
        handler.handleLocation(message, principal);

        // then - 친구 토픽은 발행, 협동러닝 토픽은 미발행
        verify(messagingTemplate).convertAndSend(eq("/topic/location/10"), any(Object.class));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/artrun/100"), any(Object.class));
    }

    @Test
    @DisplayName("artRunSessionId가 없으면 참가자 검증/협동러닝 발행을 하지 않는다")
    void handleLocation_noArtRunSessionId_doesNotPublishArtRunTopic() {
        // given - artRunSessionId 없음
        WebSocketMessage<LocationUpdateReq> message = locationMessage(null);

        // when
        handler.handleLocation(message, principal);

        // then - 친구 토픽만 발행, 참가자 검증 호출 안 됨
        verify(messagingTemplate).convertAndSend(eq("/topic/location/10"), any(Object.class));
        verify(artRunService, never()).isParticipant(any(), any());
    }

    @Test
    @DisplayName("인증 정보가 없으면 아무 동작도 하지 않는다")
    void handleLocation_nullPrincipal_doesNothing() {
        // given
        WebSocketMessage<LocationUpdateReq> message = locationMessage(100L);

        // when
        handler.handleLocation(message, null);

        // then
        verifyNoInteractions(messagingTemplate, locationService, artRunService, runSessionService);
    }

    private WebSocketMessage<LocationUpdateReq> locationMessage(Long artRunSessionId) {
        LocationUpdateReq data = new LocationUpdateReq();
        ReflectionTestUtils.setField(data, "latitude", 37.5665);
        ReflectionTestUtils.setField(data, "longitude", 126.9780);
        ReflectionTestUtils.setField(data, "artRunSessionId", artRunSessionId);
        return WebSocketMessage.of("LOCATION_UPDATE", data);
    }
}
