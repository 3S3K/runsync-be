package com._s3k.runsync.entity;

import com._s3k.runsync.domain.run.exception.RunSessionErrorCode;
import com._s3k.runsync.entity.enums.RunningSessionStatus;
import com._s3k.runsync.global.exception.GlobalException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "running_session",
        indexes = @Index(name = "idx_running_session_art_run_session_id", columnList = "art_run_session_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunningSession extends BaseEntity {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final long STALE_THRESHOLD_HOURS = 12;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RunningSessionStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_distance", precision = 5, scale = 2)
    private BigDecimal totalDistance;

    @Column(name = "last_location", columnDefinition = "geometry(Point, 4326)")
    private Point lastLocation;

    @Column(name = "current_duration_time")
    private Integer currentDurationTime;

    @Column(name = "art_run_session_id")
    private Long artRunSessionId;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Builder(access = AccessLevel.PRIVATE)
    private RunningSession(User user, LocalDateTime startTime, Long artRunSessionId) {
        this.user = user;
        this.startTime = startTime;
        this.status = RunningSessionStatus.ACTIVE;
        this.totalDistance = BigDecimal.ZERO;
        this.artRunSessionId = artRunSessionId;
    }

    public static RunningSession of(User user, LocalDateTime startTime) {
        return of(user, startTime, null);
    }

    public static RunningSession of(User user, LocalDateTime startTime, Long artRunSessionId) {
        return RunningSession.builder()
                .user(user)
                .startTime(startTime)
                .artRunSessionId(artRunSessionId)
                .build();
    }

    public void complete(LocalDateTime endTime, BigDecimal totalDistance) {
        this.status = RunningSessionStatus.COMPLETED;
        this.endTime = endTime;
        this.totalDistance = totalDistance;
    }

    public void updateLocation(double latitude, double longitude, BigDecimal currentDistance, Integer currentDurationTime) {
        this.lastLocation = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        this.totalDistance = currentDistance;
        this.currentDurationTime = currentDurationTime;
    }

    public RunRecord createRecord(BigDecimal totalDistance) {
        return RunRecord.of(
                this.user,
                this,
                this.currentDurationTime != null ? this.currentDurationTime : 0,
                this.startTime,
                totalDistance,
                null, null, null, null, null
        );
    }

    public void validateOwner(Long userId) {
        if (!Objects.equals(this.userId, userId)) {
            throw new GlobalException(RunSessionErrorCode.SESSION_NOT_OWNER);
        }
    }

    public void validateActive() {
        if (this.status != RunningSessionStatus.ACTIVE) {
            throw new GlobalException(RunSessionErrorCode.SESSION_NOT_ACTIVE);
        }
    }

    public void validateCompleted() {
        if (this.status != RunningSessionStatus.COMPLETED) {
            throw new GlobalException(RunSessionErrorCode.SESSION_NOT_COMPLETED);
        }
    }

    public void pause() {
        this.status = RunningSessionStatus.PAUSED;
    }

    public void resume() {
        this.status = RunningSessionStatus.ACTIVE;
    }

    public boolean isStale(LocalDateTime now) {
        return this.startTime.isBefore(now.minusHours(STALE_THRESHOLD_HOURS));
    }

    public void abandon() {
        this.status = RunningSessionStatus.ABANDONED;
    }

}
