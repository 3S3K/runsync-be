package com._s3k.runsync.domain.location.handler;

import com._s3k.runsync.domain.run.service.RunSessionService;
import com._s3k.runsync.global.exception.GlobalException;
import com._s3k.runsync.global.websocket.dto.WebSocketMessage;
import com._s3k.runsync.domain.location.dto.request.LocationUpdateReq;
import com._s3k.runsync.domain.location.dto.response.FriendLocationRes;
import com._s3k.runsync.domain.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class LocationMessageHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final LocationService locationService;
    private final RunSessionService runSessionService;

    @MessageMapping("/location")
    public void handleLocation(WebSocketMessage<LocationUpdateReq> message, Principal principal) {
        if (principal == null) return;
        Long userId = Long.parseLong(principal.getName());
        LocationUpdateReq data = message.getData();

        locationService.saveGeoLocation(userId, data);
        if (data.getSessionId() != null) {
            runSessionService.appendPath(
                    data.getSessionId(),
                    data.getLatitude(),
                    data.getLongitude(),
                    data.getSpeed() != null ? data.getSpeed() : 0.0
            );
        }

        Set<String> friendIds = locationService.getFriendIds(userId);
        if (friendIds.isEmpty()) return;

        FriendLocationRes response = FriendLocationRes.of(userId, data.getLatitude(), data.getLongitude());

        for (String friendId : friendIds) {
            messagingTemplate.convertAndSendToUser(
                    friendId,
                    "/queue/location",
                    WebSocketMessage.of("FRIEND_LOCATION_UPDATE", response)
            );
        }
    }

    @MessageMapping("/ping")
    @SendToUser("/queue/ping")
    public WebSocketMessage<Void> handlePing() {
        return WebSocketMessage.of("PONG", null);
    }

    @MessageExceptionHandler(GlobalException.class)
    @SendToUser("/queue/errors")
    public WebSocketMessage<String> handleGlobalException(GlobalException e) {
        return WebSocketMessage.of("ERROR", e.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public WebSocketMessage<String> handleException(Exception e) {
        return WebSocketMessage.of("ERROR", "서버 오류가 발생했습니다.");
    }
}
