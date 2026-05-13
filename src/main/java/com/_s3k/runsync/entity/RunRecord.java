package com._s3k.runsync.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "run_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "running_session_id")
    private RunningSession runningSession;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "distance", precision = 5, scale = 2)
    private BigDecimal distance;

    @Column(name = "average_pace", precision = 5, scale = 2)
    private BigDecimal averagePace;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "elevation_gain", precision = 7, scale = 2)
    private BigDecimal elevationGain;

    @Column(name = "average_heart_rate")
    private Integer averageHeartRate;

    @Column(name = "cadence")
    private Integer cadence;

    @OneToMany(mappedBy = "runRecord", cascade = CascadeType.PERSIST)
    private List<RunPath> paths = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private RunRecord(User user, RunningSession runningSession, Integer durationSeconds,
                      LocalDateTime startTime, BigDecimal distance, BigDecimal averagePace,
                      Integer calories, BigDecimal elevationGain, Integer averageHeartRate,
                      Integer cadence) {
        this.user = user;
        this.runningSession = runningSession;
        this.durationSeconds = durationSeconds;
        this.startTime = startTime;
        this.distance = distance;
        this.averagePace = averagePace;
        this.calories = calories;
        this.elevationGain = elevationGain;
        this.averageHeartRate = averageHeartRate;
        this.cadence = cadence;
        this.paths = new ArrayList<>();
    }

    public void addPaths(List<RunPath> runPaths) {
        this.paths.addAll(runPaths);
    }

    public static RunRecord of(User user, RunningSession runningSession, Integer durationSeconds,
                               LocalDateTime startTime, BigDecimal distance, BigDecimal averagePace,
                               Integer calories, BigDecimal elevationGain, Integer averageHeartRate,
                               Integer cadence) {
        return RunRecord.builder()
                .user(user)
                .runningSession(runningSession)
                .durationSeconds(durationSeconds)
                .startTime(startTime)
                .distance(distance)
                .averagePace(averagePace)
                .calories(calories)
                .elevationGain(elevationGain)
                .averageHeartRate(averageHeartRate)
                .cadence(cadence)
                .build();
    }
}
