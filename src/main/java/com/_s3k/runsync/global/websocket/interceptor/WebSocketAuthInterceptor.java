package com._s3k.runsync.global.websocket.interceptor;

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

    private final JwtValidator jwtValidator;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
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

        return message;
    }
}
