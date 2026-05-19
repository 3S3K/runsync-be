package com._s3k.runsync.entity;

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

@Entity
@Table(name = "run_paths", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"record_id", "sequence"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunPath extends BaseEntity {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private RunRecord runRecord;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    @Column(nullable = false)
    private Integer sequence;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "speed_kmh", precision = 5, scale = 2)
    private BigDecimal speedKmh;

    @Column(name = "pace_min_per_km", precision = 5, scale = 2)
    private BigDecimal paceMinPerKm;

    @Builder(access = AccessLevel.PRIVATE)
    private RunPath(RunRecord runRecord, Point location, Integer sequence, LocalDateTime recordedAt,
                    BigDecimal speedKmh, BigDecimal paceMinPerKm) {
        this.runRecord = runRecord;
        this.location = location;
        this.sequence = sequence;
        this.recordedAt = recordedAt;
        this.speedKmh = speedKmh;
        this.paceMinPerKm = paceMinPerKm;
    }

    public static RunPath of(RunRecord runRecord, double latitude, double longitude, Integer sequence,
                             LocalDateTime recordedAt, BigDecimal speedKmh, BigDecimal paceMinPerKm) {
        return RunPath.builder()
                .runRecord(runRecord)
                .location(GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude)))
                .sequence(sequence)
                .recordedAt(recordedAt)
                .speedKmh(speedKmh)
                .paceMinPerKm(paceMinPerKm)
                .build();
    }
}
