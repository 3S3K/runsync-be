package com._s3k.runsync.domain.location.handler;

import com._s3k.runsync.domain.location.dto.response.FriendStatusRes;
import com._s3k.runsync.domain.location.service.LocationService;
import com._s3k.runsync.entity.enums.FriendStatus;
import com._s3k.runsync.global.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;

@Slf4j
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
        notifyFriends(userId, FriendStatus.RUNNING);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();
        if (principal == null) return;

        Long userId = Long.parseLong(principal.getName());
        locationService.removeLocation(userId);
        notifyFriends(userId, FriendStatus.OFFLINE);
    }

    private void notifyFriends(Long userId, FriendStatus status) {
        Set<String> friendIds;
        try {
            friendIds = locationService.getFriendIds(userId);
        } catch (Exception e) {
            // 친구 목록 기능 미구현 상태에서 예외 발생 시 연결이 끊기는 것을 방지 (임시)
            log.warn("친구 목록 조회 실패 userId={}: {}", userId, e.getMessage());
            return;
        }
        if (friendIds == null || friendIds.isEmpty()) return;

        FriendStatusRes statusData = FriendStatusRes.of(userId, status);

        for (String friendId : friendIds) {
            messagingTemplate.convertAndSendToUser(
                    friendId,
                    "/queue/status",
                    WebSocketMessage.of("FRIEND_STATUS_CHANGE", statusData)
            );
        }
    }
}
