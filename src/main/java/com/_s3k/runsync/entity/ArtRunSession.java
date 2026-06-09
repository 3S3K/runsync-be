package com._s3k.runsync.entity;

import com._s3k.runsync.domain.artrun.exception.ArtRunErrorCode;
import com._s3k.runsync.entity.enums.ArtRunStatus;
import com._s3k.runsync.global.exception.GlobalException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "art_run_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtRunSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArtRunStatus status;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "meeting_time", nullable = false)
    private LocalDateTime meetingTime;

    @Column(name = "meeting_place_name", nullable = false)
    private String meetingPlaceName;

    @Column(name = "meeting_point", columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point meetingPoint;

    @Column(name = "route_path", columnDefinition = "geometry(LineString, 4326)", nullable = false)
    private LineString routePath;

    @Builder(access = AccessLevel.PRIVATE)
    private ArtRunSession(User host, String title, Integer capacity, LocalDateTime meetingTime,
                          String meetingPlaceName, Point meetingPoint, LineString routePath) {
        this.host = host;
        this.title = title;
        this.capacity = capacity;
        this.meetingTime = meetingTime;
        this.meetingPlaceName = meetingPlaceName;
        this.meetingPoint = meetingPoint;
        this.routePath = routePath;
        this.status = ArtRunStatus.RECRUITING;
    }

    public static ArtRunSession of(User host, String title, Integer capacity, LocalDateTime meetingTime,
                                   String meetingPlaceName, Point meetingPoint, LineString routePath) {
        return ArtRunSession.builder()
                .host(host)
                .title(title)
                .capacity(capacity)
                .meetingTime(meetingTime)
                .meetingPlaceName(meetingPlaceName)
                .meetingPoint(meetingPoint)
                .routePath(routePath)
                .build();
    }

    public boolean isRecruiting() {
        return this.status == ArtRunStatus.RECRUITING;
    }

    public boolean isFull(int currentCount) {
        return currentCount >= this.capacity;
    }

    public boolean isHost(Long userId) {
        return this.host.getId().equals(userId);
    }

    public void start() {
        if (this.status != ArtRunStatus.RECRUITING) {
            throw new GlobalException(ArtRunErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ArtRunStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.status != ArtRunStatus.IN_PROGRESS) {
            throw new GlobalException(ArtRunErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = ArtRunStatus.COMPLETED;
    }

    public void validateHost(Long userId) {
        if (!isHost(userId)) {
            throw new GlobalException(ArtRunErrorCode.NOT_HOST);
        }
    }

    public void validateInProgress() {
        if (this.status != ArtRunStatus.IN_PROGRESS) {
            throw new GlobalException(ArtRunErrorCode.ARTRUN_NOT_IN_PROGRESS);
        }
    }
}
