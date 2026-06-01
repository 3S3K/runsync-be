package com._s3k.runsync.global.websocket.interceptor;

import com._s3k.runsync.domain.location.service.LocationService;
import com._s3k.runsync.global.security.jwt.JwtValidator;
import com._s3k.runsync.global.security.jwt.dto.JwtUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String FRIEND_TOPIC_PATTERN = "^/topic/(status|location)/\\d+$";

    private final JwtValidator jwtValidator;
    private final LocationService locationService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("JWT 토큰이 없습니다.");
            }

            String token = authHeader.substring(7);

            if (!jwtValidator.validateToken(token)) {
                throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.");
            }

            JwtUserInfo userInfo = jwtValidator.getUserIdAndRole(token);
            accessor.setUser(() -> String.valueOf(userInfo.getUserId()));
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination == null) return message;

            boolean isProtectedTopic = destination.equals("/topic/status") || destination.startsWith("/topic/status/")
                    || destination.equals("/topic/location") || destination.startsWith("/topic/location/");
            boolean isValidProtectedTopic = destination.matches(FRIEND_TOPIC_PATTERN);

            if (isProtectedTopic && !isValidProtectedTopic) {
                throw new IllegalArgumentException("잘못된 구독 경로입니다.");
            }

            if (isValidProtectedTopic) {
                if (accessor.getUser() == null) {
                    throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
                }

                String targetUserId = destination.substring(destination.lastIndexOf('/') + 1);
                String subscriberUserId = accessor.getUser().getName();
                if (subscriberUserId.equals(targetUserId)) {
                    throw new IllegalArgumentException("자기 자신을 구독할 수 없습니다.");
                }

                if (!locationService.canSubscribeFriendTopic(Long.parseLong(subscriberUserId), Long.parseLong(targetUserId))) {
                    throw new IllegalArgumentException("친구가 아닌 사용자의 상태/위치를 구독할 수 없습니다.");
                }
            }
        }

        return message;
    }
}
