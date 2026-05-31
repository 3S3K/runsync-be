package com._s3k.runsync.domain.run.dto.response;

import com._s3k.runsync.entity.RunPath;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "GPS 경로 포인트")
public class RunPathRes {

    @Schema(description = "좌표 순서", example = "1")
    private final Integer sequence;

    @Schema(description = "위도", example = "37.1234")
    private final Double latitude;

    @Schema(description = "경도", example = "127.5678")
    private final Double longitude;

    @Schema(description = "기록 시간", example = "2026-03-01T09:00:05")
    private final LocalDateTime recordedAt;

    @Schema(description = "속도 (km/h)", example = "8.50")
    private final Double speedKmh;

    @Schema(description = "페이스 (분/km)", example = "7.05")
    private final Double paceMinPerKm;

    private RunPathRes(Integer sequence, Double latitude, Double longitude, LocalDateTime recordedAt,
                       Double speedKmh, Double paceMinPerKm) {
        this.sequence = sequence;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = recordedAt;
        this.speedKmh = speedKmh;
        this.paceMinPerKm = paceMinPerKm;
    }

    public static RunPathRes of(RunPath path) {
        return new RunPathRes(
                path.getSequence(),
                path.getLocation().getY(),
                path.getLocation().getX(),
                path.getRecordedAt(),
                path.getSpeedKmh() != null ? path.getSpeedKmh().doubleValue() : null,
                path.getPaceMinPerKm() != null ? path.getPaceMinPerKm().doubleValue() : null
        );
    }
}
