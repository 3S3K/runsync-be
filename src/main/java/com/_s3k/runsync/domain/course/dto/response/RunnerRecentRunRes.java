package com._s3k.runsync.domain.course.dto.response;

import com._s3k.runsync.entity.RunRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Schema(description = "최근 러닝 한 회차")
public class RunnerRecentRunRes {

    @Schema(description = "달린 날짜", example = "2026-11-12")
    private final LocalDate date;

    @Schema(description = "달린 거리(km)", example = "5.0")
    private final Double distanceKm;

    private RunnerRecentRunRes(LocalDate date, Double distanceKm) {
        this.date = date;
        this.distanceKm = distanceKm;
    }

    public static RunnerRecentRunRes of(RunRecord record) {
        return new RunnerRecentRunRes(record.getStartTime().toLocalDate(), record.getDistance().doubleValue());
    }
}
