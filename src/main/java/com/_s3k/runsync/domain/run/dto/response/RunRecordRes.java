package com._s3k.runsync.domain.run.dto.response;

import com._s3k.runsync.entity.RunRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Schema(description = "러닝 기록 상세 응답")
public class RunRecordRes {

    @Schema(description = "기록 ID", example = "1")
    private final Long recordId;

    @Schema(description = "러닝 시작 시간", example = "2026-03-01T09:00:00")
    private final LocalDateTime startTime;

    @Schema(description = "소요 시간 (초)", example = "1048")
    private final Integer durationSeconds;

    @Schema(description = "이동 거리 (km)", example = "5.20")
    private final Double distance;

    @Schema(description = "평균 페이스 (min/km)", example = "6.43")
    private final Double averagePace;

    @Schema(description = "소모 칼로리 (kcal)", example = "134")
    private final Integer calories;

    @Schema(description = "케이던스 (spm)", example = "167")
    private final Integer cadence;

    @Schema(description = "고도 상승 (m)", example = "12.50")
    private final Double elevationGain;

    @Schema(description = "평균 심박수 (bpm)", example = "172")
    private final Integer averageHeartRate;

    @Schema(description = "GPS 경로 포인트 배열, 경로 없는 경우 빈 배열")
    private final List<RunPathRes> paths;

    private RunRecordRes(Long recordId, LocalDateTime startTime, Integer durationSeconds,
                         Double distance, Double averagePace, Integer calories, Integer cadence,
                         Double elevationGain, Integer averageHeartRate, List<RunPathRes> paths) {
        this.recordId = recordId;
        this.startTime = startTime;
        this.durationSeconds = durationSeconds;
        this.distance = distance;
        this.averagePace = averagePace;
        this.calories = calories;
        this.cadence = cadence;
        this.elevationGain = elevationGain;
        this.averageHeartRate = averageHeartRate;
        this.paths = paths;
    }

    public static RunRecordRes of(RunRecord record) {
        return new RunRecordRes(
                record.getId(),
                record.getStartTime(),
                record.getDurationSeconds(),
                record.getDistance().doubleValue(),
                record.getAveragePace() != null ? record.getAveragePace().doubleValue() : null,
                record.getCalories(),
                record.getCadence(),
                record.getElevationGain() != null ? record.getElevationGain().doubleValue() : null,
                record.getAverageHeartRate(),
                record.getPaths().stream().map(RunPathRes::of).toList()
        );
    }
}
