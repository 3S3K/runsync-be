package com._s3k.runsync.global.websocket.interceptor;

import com._s3k.runsync.domain.artrun.service.ArtRunService;
import com._s3k.runsync.domain.location.service.LocationService;
import com._s3k.runsync.global.security.jwt.JwtValidator;
import com._s3k.runsync.global.security.jwt.dto.JwtUserInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

    @InjectMocks
    private WebSocketAuthInterceptor interceptor;

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private LocationService locationService;

    @Mock
    private ArtRunService artRunService;

    @Mock
    private MessageChannel channel;

    private Message<?> buildSubscribeMessage(String destination, String principalName) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        if (principalName != null) {
            accessor.setUser(() -> principalName);
        }
        accessor.setSessionId("test-session");
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("친구 topic 정상 구독 허용")
    void subscribe_validFriendTopic_allowed() {
        // given
        given(locationService.canSubscribeFriendTopic(1L, 2L)).willReturn(true);
        Message<?> message = buildSubscribeMessage("/topic/status/2", "1");

        // when
        Message<?> result = interceptor.preSend(message, channel);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("친구가 아닌 사용자 구독 거부")
    void subscribe_notFriend_throwsException() {
        // given
        given(locationService.canSubscribeFriendTopic(1L, 2L)).willReturn(false);
        Message<?> message = buildSubscribeMessage("/topic/location/2", "1");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("친구가 아닌 사용자의 상태/위치를 구독할 수 없습니다.");
    }

    @Test
    @DisplayName("자기 자신 구독 거부")
    void subscribe_selfSubscription_throwsException() {
        // given
        Message<?> message = buildSubscribeMessage("/topic/status/1", "1");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("자기 자신을 구독할 수 없습니다.");
    }

    @Test
    @DisplayName("malformed topic 거부")
    void subscribe_malformedTopic_throwsException() {
        // given
        Message<?> message = buildSubscribeMessage("/topic/status/abc", "1");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잘못된 구독 경로입니다.");
    }

    @Test
    @DisplayName("인증 없는 친구 topic 구독 거부")
    void subscribe_noAuthentication_throwsException() {
        // given
        Message<?> message = buildSubscribeMessage("/topic/status/2", null);

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증되지 않은 사용자입니다.");
    }

    @Test
    @DisplayName("협동 러닝 세션 topic 정상 구독 허용 - 참가자")
    void subscribe_validArtRunTopic_allowed() {
        // given
        given(artRunService.isParticipant(1L, 100L)).willReturn(true);
        Message<?> message = buildSubscribeMessage("/topic/artrun/100", "1");

        // when
        Message<?> result = interceptor.preSend(message, channel);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("참가자가 아닌 사용자의 협동 러닝 세션 topic 구독 거부")
    void subscribe_notParticipant_throwsException() {
        // given
        given(artRunService.isParticipant(1L, 100L)).willReturn(false);
        Message<?> message = buildSubscribeMessage("/topic/artrun/100", "1");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("세션 참가자가 아니면 구독할 수 없습니다.");
    }

    @Test
    @DisplayName("malformed 협동 러닝 topic 거부")
    void subscribe_malformedArtRunTopic_throwsException() {
        // given
        Message<?> message = buildSubscribeMessage("/topic/artrun/abc", "1");

        // when & then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("잘못된 구독 경로입니다.");
    }

    @Test
    @DisplayName("/user/queue/ping 구독 시 영향 없음")
    void subscribe_pingQueue_notAffected() {
        // given
        Message<?> message = buildSubscribeMessage("/user/queue/ping", "1");

        // when
        Message<?> result = interceptor.preSend(message, channel);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("/user/queue/errors 구독 시 영향 없음")
    void subscribe_errorsQueue_notAffected() {
        // given
        Message<?> message = buildSubscribeMessage("/user/queue/errors", "1");

        // when
        Message<?> result = interceptor.preSend(message, channel);

        // then
        assertThat(result).isNotNull();
    }
}
