package com._s3k.runsync.entity;

import com._s3k.runsync.entity.enums.RunningSessionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "running_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunningSession extends BaseEntity {

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

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Builder(access = AccessLevel.PRIVATE)
    private RunningSession(User user, LocalDateTime startTime) {
        this.user = user;
        this.startTime = startTime;
        this.status = RunningSessionStatus.ACTIVE;
        this.totalDistance = BigDecimal.ZERO;
    }

    public static RunningSession of(User user, LocalDateTime startTime) {
        return RunningSession.builder()
                .user(user)
                .startTime(startTime)
                .build();
    }

    public void complete(LocalDateTime endTime, BigDecimal totalDistance) {
        this.status = RunningSessionStatus.COMPLETED;
        this.endTime = endTime;
        this.totalDistance = totalDistance;
    }

    public void updateLocation(Point point, BigDecimal currentDistance, Integer currentDurationTime) {
        this.lastLocation = point;
        this.totalDistance = currentDistance; // 러닝 완료 전까지 현재 거리를 totalDistance에 누적, 종료 시 최종값으로 덮어씀
        this.currentDurationTime = currentDurationTime;
    }

    public void pause() {
        this.status = RunningSessionStatus.PAUSED;
    }

    public void resume() {
        this.status = RunningSessionStatus.ACTIVE;
    }

}
