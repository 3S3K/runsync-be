package com._s3k.runsync.domain.location.handler;

import com._s3k.runsync.domain.location.dto.response.ActivityStatusRes;
import com._s3k.runsync.domain.location.service.LocationService;
import com._s3k.runsync.entity.enums.ActivityStatus;
import com._s3k.runsync.global.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final LocationService locationService;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null) return;

        Long userId = Long.parseLong(principal.getName());
        messagingTemplate.convertAndSend(
                "/topic/status/" + userId,
                WebSocketMessage.of("FRIEND_STATUS_CHANGE", ActivityStatusRes.of(userId, ActivityStatus.RUNNING))
        );
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null) return;

        Long userId = Long.parseLong(principal.getName());
        locationService.removeLocation(userId);
        messagingTemplate.convertAndSend(
                "/topic/status/" + userId,
                WebSocketMessage.of("FRIEND_STATUS_CHANGE", ActivityStatusRes.of(userId, ActivityStatus.OFFLINE))
        );
    }
}
