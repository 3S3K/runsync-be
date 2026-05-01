package com._s3k.runsync.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Record extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "running_session_id")
    private RunningSession runningSession;

    @Column(name = "duration_time", nullable = false)
    private Integer durationTime;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(precision = 5, scale = 2)
    private BigDecimal distance;

    @Column(name = "average_pace", precision = 5, scale = 2)
    private BigDecimal averagePace;

    private Integer calories;

    @Column(name = "elevation_gain", precision = 5, scale = 2)
    private BigDecimal elevationGain;

    @Column(name = "average_heart_rate")
    private Integer averageHeartRate;

    private Integer cadence;

    @Builder(access = AccessLevel.PRIVATE)
    private Record(User user, RunningSession runningSession, Integer durationTime,
                   LocalDateTime startTime, BigDecimal distance, BigDecimal averagePace,
                   Integer calories, BigDecimal elevationGain, Integer averageHeartRate,
                   Integer cadence) {
        this.user = user;
        this.runningSession = runningSession;
        this.durationTime = durationTime;
        this.startTime = startTime;
        this.distance = distance;
        this.averagePace = averagePace;
        this.calories = calories;
        this.elevationGain = elevationGain;
        this.averageHeartRate = averageHeartRate;
        this.cadence = cadence;
    }

    public static Record of(User user, RunningSession runningSession, Integer durationTime,
                            LocalDateTime startTime, BigDecimal distance, BigDecimal averagePace,
                            Integer calories, BigDecimal elevationGain, Integer averageHeartRate,
                            Integer cadence) {
        return Record.builder()
                .user(user)
                .runningSession(runningSession)
                .durationTime(durationTime)
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
