package com._s3k.runsync.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "run_paths", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"record_id", "sequence"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunPath extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private RunRecord runRecord;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    @Column(nullable = false)
    private Integer sequence;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "altitude_m", precision = 7, scale = 2)
    private BigDecimal altitudeM;

    @Column(name = "speed_kmh", precision = 5, scale = 2)
    private BigDecimal speedKmh;

    @Column(name = "pace_min_per_km", precision = 5, scale = 2)
    private BigDecimal paceMinPerKm;

    @Column(name = "accuracy_m", precision = 6, scale = 2)
    private BigDecimal accuracyM;

    @Builder(access = AccessLevel.PRIVATE)
    private RunPath(RunRecord runRecord, Point location, Integer sequence, LocalDateTime recordedAt,
                    BigDecimal altitudeM, BigDecimal speedKmh, BigDecimal paceMinPerKm, BigDecimal accuracyM) {
        this.runRecord = runRecord;
        this.location = location;
        this.sequence = sequence;
        this.recordedAt = recordedAt;
        this.altitudeM = altitudeM;
        this.speedKmh = speedKmh;
        this.paceMinPerKm = paceMinPerKm;
        this.accuracyM = accuracyM;
    }

    public static RunPath of(RunRecord runRecord, Point location, Integer sequence, LocalDateTime recordedAt,
                             BigDecimal altitudeM, BigDecimal speedKmh, BigDecimal paceMinPerKm, BigDecimal accuracyM) {
        return RunPath.builder()
                .runRecord(runRecord)
                .location(location)
                .sequence(sequence)
                .recordedAt(recordedAt)
                .altitudeM(altitudeM)
                .speedKmh(speedKmh)
                .paceMinPerKm(paceMinPerKm)
                .accuracyM(accuracyM)
                .build();
    }
}
