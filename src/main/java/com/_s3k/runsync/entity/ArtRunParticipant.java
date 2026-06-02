package com._s3k.runsync.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "art_run_participant", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"art_run_session_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtRunParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_run_session_id", nullable = false)
    private ArtRunSession artRunSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private ArtRunParticipant(ArtRunSession artRunSession, User user) {
        this.artRunSession = artRunSession;
        this.user = user;
    }

    public static ArtRunParticipant of(ArtRunSession artRunSession, User user) {
        return ArtRunParticipant.builder()
                .artRunSession(artRunSession)
                .user(user)
                .build();
    }
}
