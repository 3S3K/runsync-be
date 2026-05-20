package com._s3k.runsync.domain.users.dto.response;

import com._s3k.runsync.entity.RunRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "러닝 기록")
public class RecordRes {

    @Schema(description = "기록 ID", example = "1")
    private Long recordId;
    @Schema(description = "러닝 시작 시간", example = "2026-03-01T09:00:00")
    private LocalDateTime startTime;
    @Schema(description = "소요 시간 (초)", example = "1048")
    private Integer durationSeconds;
    @Schema(description = "이동 거리 (km)", example = "5.20")
    private Double distance;
    @Schema(description = "평균 페이스", example = "6.43")
    private Double averagePace;
    @Schema(description = "소모 칼로리", example = "134")
    private Integer calories;
    @Schema(description = "케이던스", example = "167")
    private Integer cadence;
    @Schema(description = "고도 상승 (m)", example = "12.50")
    private Double elevationGain;
    @Schema(description = "평균 심박수", example = "172")
    private Integer averageHeartRate;

    private RecordRes(Long recordId, LocalDateTime startTime, Integer durationSeconds,
                      Double distance, Double averagePace, Integer calories,
                      Integer cadence, Double elevationGain, Integer averageHeartRate) {
        this.recordId = recordId;
        this.startTime = startTime;
        this.durationSeconds = durationSeconds;
        this.distance = distance;
        this.averagePace = averagePace;
        this.calories = calories;
        this.cadence = cadence;
        this.elevationGain = elevationGain;
        this.averageHeartRate = averageHeartRate;
    }

    public static RecordRes of(RunRecord record) {
        return new RecordRes(
                record.getId(),
                record.getStartTime(),
                record.getDurationSeconds(),
                record.getDistance() != null ? record.getDistance().doubleValue() : null,
                record.getAveragePace() != null ? record.getAveragePace().doubleValue() : null,
                record.getCalories(),
                record.getCadence(),
                record.getElevationGain() != null ? record.getElevationGain().doubleValue() : null,
                record.getAverageHeartRate()
        );
    }
}
