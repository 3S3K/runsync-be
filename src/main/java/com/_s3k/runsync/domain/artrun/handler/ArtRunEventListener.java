package com._s3k.runsync.domain.artrun.handler;

import com._s3k.runsync.domain.artrun.event.ArtRunParticipantLeftEvent;
import com._s3k.runsync.domain.artrun.event.ArtRunSessionClosedEvent;
import com._s3k.runsync.global.websocket.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ArtRunEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    public void onSessionClosed(ArtRunSessionClosedEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/artrun/" + event.sessionId(),
                WebSocketMessage.of("ARTRUN_SESSION_CLOSED", event.sessionId())
        );
    }

    @TransactionalEventListener
    public void onParticipantLeft(ArtRunParticipantLeftEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/artrun/" + event.sessionId(),
                WebSocketMessage.of("ARTRUN_PARTICIPANT_LEFT", event.userId())
        );
    }
}
