package com._s3k.runsync.domain.artrun.handler;

import com._s3k.runsync.domain.artrun.event.ArtRunParticipantLeftEvent;
import com._s3k.runsync.domain.artrun.event.ArtRunSessionClosedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArtRunEventListenerTest {

    @InjectMocks
    private ArtRunEventListener listener;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("세션 종료 이벤트 → /topic/artrun/{id}로 발행")
    void onSessionClosed_broadcasts() {
        listener.onSessionClosed(new ArtRunSessionClosedEvent(8L));

        verify(messagingTemplate).convertAndSend(eq("/topic/artrun/8"), any(Object.class));
    }

    @Test
    @DisplayName("참가자 이탈 이벤트 → /topic/artrun/{id}로 발행")
    void onParticipantLeft_broadcasts() {
        listener.onParticipantLeft(new ArtRunParticipantLeftEvent(8L, 10L));

        verify(messagingTemplate).convertAndSend(eq("/topic/artrun/8"), any(Object.class));
    }
}
