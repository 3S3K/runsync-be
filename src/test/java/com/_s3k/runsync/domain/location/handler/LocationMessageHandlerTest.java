package com._s3k.runsync.domain.location.handler;

import com._s3k.runsync.domain.location.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.location.service.LocationService;
import com._s3k.runsync.domain.run.service.RunSessionService;
import com._s3k.runsync.global.websocket.dto.WebSocketMessage;
import com._s3k.runsync.global.websocket.interceptor.WebSocketAuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private final Principal principal = () -> "10";

    @Test
    @DisplayName("협동 러닝 토픽 구독자가 위치를 보내면 /topic/artrun/{id}로도 발행된다")
    void handleLocation_subscribedToArtRun_publishesArtRunTopic() {
        // given - 세션 속성에 sessionId 100 구독 기록 있음
        WebSocketMessage<LocationUpdateReq> message = locationMessage(100L);

        // when
        handler.handleLocation(message, principal, headerAccessor(100L));

        // then
        verify(messagingTemplate).convertAndSend(eq("/topic/location/10"), any(Object.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/artrun/100"), any(Object.class));
    }

    @Test
    @DisplayName("협동 러닝 토픽 구독 기록이 없으면 /topic/artrun/{id}로 발행되지 않는다")
    void handleLocation_notSubscribed_doesNotPublishArtRunTopic() {
        // given - 세션 속성에 구독 기록 없음
        WebSocketMessage<LocationUpdateReq> message = locationMessage(100L);

        // when
        handler.handleLocation(message, principal, headerAccessor(null));

        // then
        verify(messagingTemplate).convertAndSend(eq("/topic/location/10"), any(Object.class));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/artrun/100"), any(Object.class));
    }

    @Test
    @DisplayName("artRunSessionId가 없으면 협동러닝 토픽으로 발행하지 않는다")
    void handleLocation_noArtRunSessionId_doesNotPublishArtRunTopic() {
        // given - artRunSessionId 없음 (구독 기록은 있어도 무관)
        WebSocketMessage<LocationUpdateReq> message = locationMessage(null);

        // when
        handler.handleLocation(message, principal, headerAccessor(100L));

        // then
        verify(messagingTemplate).convertAndSend(eq("/topic/location/10"), any(Object.class));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/artrun/100"), any(Object.class));
    }

    @Test
    @DisplayName("인증 정보가 없으면 아무 동작도 하지 않는다")
    void handleLocation_nullPrincipal_doesNothing() {
        // given
        WebSocketMessage<LocationUpdateReq> message = locationMessage(100L);

        // when
        handler.handleLocation(message, null, headerAccessor(100L));

        // then
        verifyNoInteractions(messagingTemplate, locationService, runSessionService);
    }

    private WebSocketMessage<LocationUpdateReq> locationMessage(Long artRunSessionId) {
        LocationUpdateReq data = new LocationUpdateReq();
        ReflectionTestUtils.setField(data, "latitude", 37.5665);
        ReflectionTestUtils.setField(data, "longitude", 126.9780);
        ReflectionTestUtils.setField(data, "artRunSessionId", artRunSessionId);
        return WebSocketMessage.of("LOCATION_UPDATE", data);
    }

    private SimpMessageHeaderAccessor headerAccessor(Long subscribedSessionId) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> attributes = new HashMap<>();
        if (subscribedSessionId != null) {
            Map<String, Long> subscriptions = new ConcurrentHashMap<>();
            subscriptions.put("sub-0", subscribedSessionId);
            attributes.put(WebSocketAuthInterceptor.ARTRUN_SUBSCRIPTIONS, subscriptions);
        }
        accessor.setSessionAttributes(attributes);
        return accessor;
    }
}
