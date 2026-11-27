package com._s3k.runsync.domain.course.dto.response;

import com._s3k.runsync.entity.enums.CourseIntensity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "추천 코스")
public class CourseRes {

    @Schema(description = "순위 (1이 가장 좋음)", example = "1")
    private final Integer rank;

    @Schema(description = "코스 총 거리(km)", example = "5.12")
    private final Double distanceKm;

    @Schema(description = "누적 오르막(m)", example = "38.0")
    private final Double elevationGainM;

    @Schema(description = "가장 가파른 오르막 경사(%)", example = "6.4")
    private final Double maxSlopePct;

    @Schema(description = "예상 운동 강도 (최대 심박 대비 %)", example = "74.5")
    private final Double predictedHrPct;

    @Schema(description = "요청한 강도", example = "NORMAL")
    private final CourseIntensity intensity;

    @Schema(description = "추천 이유", example = "목표 5km 기준 5.1km 순환 코스예요.")
    private final String reason;

    @Schema(description = "코스 경로")
    private final CourseGeometryRes geometry;

    private CourseRes(Integer rank, Double distanceKm, Double elevationGainM, Double maxSlopePct,
                      Double predictedHrPct, CourseIntensity intensity, String reason,
                      CourseGeometryRes geometry) {
        this.rank = rank;
        this.distanceKm = distanceKm;
        this.elevationGainM = elevationGainM;
        this.maxSlopePct = maxSlopePct;
        this.predictedHrPct = predictedHrPct;
        this.intensity = intensity;
        this.reason = reason;
        this.geometry = geometry;
    }

    public static CourseRes of(AiRecommendRes.Course course) {
        return new CourseRes(
                course.getRank(),
                course.getDistanceKm(),
                course.getElevationGainM(),
                course.getMaxSlopePct(),
                course.getPredictedHrPct(),
                course.getIntensity(),
                course.getReason(),
                CourseGeometryRes.of(course.getGeometry())
        );
    }
}
