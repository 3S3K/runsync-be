package com._s3k.runsync.global.websocket.interceptor;

import com._s3k.runsync.domain.artrun.repository.ArtRunParticipantRepository;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    public static final String ARTRUN_SUBSCRIPTIONS = "artRunSubscriptions";

    private static final String FRIEND_TOPIC_PATTERN = "^/topic/(status|location)/\\d+$";
    private static final String ARTRUN_TOPIC_PATTERN = "^/topic/artrun/\\d+$";

    private final JwtValidator jwtValidator;
    private final LocationService locationService;
    private final ArtRunParticipantRepository artRunParticipantRepository;

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

            boolean isArtRunTopic = destination.equals("/topic/artrun") || destination.startsWith("/topic/artrun/");
            boolean isValidArtRunTopic = destination.matches(ARTRUN_TOPIC_PATTERN);

            if (isArtRunTopic && !isValidArtRunTopic) {
                throw new IllegalArgumentException("잘못된 구독 경로입니다.");
            }

            if (isValidArtRunTopic) {
                if (accessor.getUser() == null) {
                    throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
                }

                Long sessionId = Long.parseLong(destination.substring(destination.lastIndexOf('/') + 1));
                Long subscriberUserId = Long.parseLong(accessor.getUser().getName());
                if (!artRunParticipantRepository.existsByArtRunSession_IdAndUser_Id(sessionId, subscriberUserId)) {
                    throw new IllegalArgumentException("세션 참가자가 아니면 구독할 수 없습니다.");
                }

                // 구독 시 검증한 참가 정보를 세션에 캐싱 → 발행 시 DB 재조회 없이 활용
                String subscriptionId = accessor.getSubscriptionId();
                if (subscriptionId == null) {
                    throw new IllegalArgumentException("구독 ID(subscriptionId)가 누락되었습니다.");
                }
                artRunSubscriptions(accessor).put(subscriptionId, sessionId);
            }
        }

        if (StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            String subscriptionId = accessor.getSubscriptionId();
            if (subscriptionId == null) {
                throw new IllegalArgumentException("구독 ID(subscriptionId)가 누락되었습니다.");
            }
            artRunSubscriptions(accessor).remove(subscriptionId);
        }

        return message;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> artRunSubscriptions(StompHeaderAccessor accessor) {
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) {
            attributes = new ConcurrentHashMap<>();
            accessor.setSessionAttributes(attributes);
        }
        return (Map<String, Long>) attributes.computeIfAbsent(ARTRUN_SUBSCRIPTIONS, k -> new ConcurrentHashMap<String, Long>());
    }
}
