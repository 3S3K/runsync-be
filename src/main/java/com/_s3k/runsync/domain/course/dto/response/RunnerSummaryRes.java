package com._s3k.runsync.domain.course.dto.response;

import com._s3k.runsync.entity.RunRecord;
import com._s3k.runsync.entity.enums.FitnessLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;

@Getter
@Schema(description = "내 러닝 분석 (코스 추천에 반영되는 최근 기록 요약)")
public class RunnerSummaryRes {

    @Schema(description = "요약에 쓴 최근 기록 수 (최대 5)", example = "5")
    private final Integer recordCount;

    @Schema(description = "평균 페이스(초/km). 기록이 없으면 null", example = "380")
    private final Integer averagePaceSecPerKm;

    @Schema(description = "평균 심박수(bpm). 심박이 기록된 러닝이 없으면 null", example = "158")
    private final Integer averageHeartRate;

    @Schema(description = "평균 케이던스(spm). 케이던스가 기록된 러닝이 없으면 null", example = "164")
    private final Integer averageCadence;

    @Schema(description = "회당 평균 거리(km). 기록이 없으면 null", example = "5.2")
    private final Double averageDistanceKm;

    @Schema(description = "오늘을 포함한 최근 7일 동안 달린 거리(km)", example = "15.9")
    private final Double weeklyDistanceKm;

    @Schema(description = "평균 페이스 기준 체력 수준. 기록이 없으면 null", example = "INTERMEDIATE")
    private final FitnessLevel fitnessLevel;

    @Schema(description = "최근 러닝 목록 (오래된 순)")
    private final List<RunnerRecentRunRes> recentRuns;

    private RunnerSummaryRes(Integer recordCount, Integer averagePaceSecPerKm, Integer averageHeartRate,
                             Integer averageCadence, Double averageDistanceKm, Double weeklyDistanceKm,
                             FitnessLevel fitnessLevel, List<RunnerRecentRunRes> recentRuns) {
        this.recordCount = recordCount;
        this.averagePaceSecPerKm = averagePaceSecPerKm;
        this.averageHeartRate = averageHeartRate;
        this.averageCadence = averageCadence;
        this.averageDistanceKm = averageDistanceKm;
        this.weeklyDistanceKm = weeklyDistanceKm;
        this.fitnessLevel = fitnessLevel;
        this.recentRuns = recentRuns;
    }

    /**
     * @param recentRecords  거리와 시간이 0보다 큰 최근 기록 (최신순)
     * @param weeklyDistance 최근 7일 동안 달린 거리(km)
     */
    public static RunnerSummaryRes of(List<RunRecord> recentRecords, BigDecimal weeklyDistance) {
        double weeklyDistanceKm = weeklyDistance.setScale(1, RoundingMode.HALF_UP).doubleValue();
        if (recentRecords.isEmpty()) {
            return new RunnerSummaryRes(0, null, null, null, null, weeklyDistanceKm, null, List.of());
        }

        BigDecimal totalDistance = recentRecords.stream()
                .map(RunRecord::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalSeconds = recentRecords.stream().mapToInt(RunRecord::getDurationSeconds).sum();

        // 추천 서버와 같은 방식: 총 시간 ÷ 총 거리
        int paceSecPerKm = (int) Math.round(totalSeconds / totalDistance.doubleValue());
        double averageDistanceKm = totalDistance
                .divide(BigDecimal.valueOf(recentRecords.size()), 1, RoundingMode.HALF_UP)
                .doubleValue();

        // 차트를 왼쪽부터 그릴 수 있도록 오래된 순으로 뒤집는다
        List<RunRecord> oldestFirst = new ArrayList<>(recentRecords);
        Collections.reverse(oldestFirst);

        return new RunnerSummaryRes(
                recentRecords.size(),
                paceSecPerKm,
                averageOfRecorded(recentRecords, RunRecord::getAverageHeartRate),
                averageOfRecorded(recentRecords, RunRecord::getCadence),
                averageDistanceKm,
                weeklyDistanceKm,
                FitnessLevel.fromPaceSecPerKm(paceSecPerKm),
                oldestFirst.stream().map(RunnerRecentRunRes::of).toList()
        );
    }

    // 값이 없거나 0인 기록은 측정되지 않은 것으로 보고 평균에서 뺀다
    private static Integer averageOfRecorded(List<RunRecord> records, Function<RunRecord, Integer> value) {
        OptionalDouble average = records.stream()
                .map(value)
                .filter(v -> v != null && v > 0)
                .mapToInt(Integer::intValue)
                .average();
        return average.isPresent() ? (int) Math.round(average.getAsDouble()) : null;
    }
}
