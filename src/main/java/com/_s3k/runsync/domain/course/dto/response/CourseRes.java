package com._s3k.runsync.domain.course.dto.response;

import com._s3k.runsync.entity.enums.CourseIntensity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "추천 코스")
public class CourseRes {

    @Schema(description = "순위 (1이 가장 좋음)", example = "1")
    private final Integer rank;

    @Schema(description = "코스 이름", example = "용운로 순환")
    private final String name;

    @Schema(description = "추천 점수 (0~100, 높을수록 조건에 잘 맞음)", example = "89")
    private final Integer score;

    @Schema(description = "코스 총 거리(km)", example = "5.12")
    private final Double distanceKm;

    @Schema(description = "누적 오르막(m)", example = "38.0")
    private final Double elevationGainM;

    @Schema(description = "가장 가파른 오르막 경사(%)", example = "6.4")
    private final Double maxSlopePct;

    @Schema(description = "예상 운동 강도 (최대 심박 대비 %)", example = "74.0")
    private final Double predictedHrPct;

    @Schema(description = "요청한 강도", example = "NORMAL")
    private final CourseIntensity intensity;

    @Schema(description = "추천 이유", example = "목표 5km 기준 5.1km 순환 코스예요.")
    private final String reason;

    @Schema(description = "코스 경로")
    private final CourseGeometryRes geometry;

    @Schema(description = "고도 프로필 (출발점부터 거리 순, 최대 100개 지점)")
    private final List<CourseElevationPointRes> elevationProfile;

    @Schema(description = "예상 소요 시간(분). 최근 기록의 평균 페이스 기준이며 기록이 없으면 null", example = "31")
    private final Integer estimatedTimeMin;

    @Schema(description = "계단 구간 수", example = "0")
    private final Integer stairsCount;

    @Schema(description = "예상 심박존 (Z1~Z5)", example = "Z3")
    private final String hrZone;

    @Schema(description = "예상 심박수 [최저, 최고](bpm). 나이를 모르면 null", example = "[142, 152]")
    private final List<Integer> hrBpmRange;

    @Schema(description = "가장 많이 오르는 구간. 오르막이 거의 없으면 null")
    private final CourseClimbRes biggestClimb;

    @Schema(description = "경사 등급별로 나눈 경로")
    private final List<CourseSlopeSegmentRes> slopeSegments;

    private CourseRes(AiRecommendRes.Course course) {
        this.rank = course.getRank();
        this.name = course.getName();
        this.score = course.getScore();
        this.distanceKm = course.getDistanceKm();
        this.elevationGainM = course.getElevationGainM();
        this.maxSlopePct = course.getMaxSlopePct();
        this.predictedHrPct = course.getPredictedHrPct();
        this.intensity = course.getIntensity();
        this.reason = course.getReason();
        this.geometry = CourseGeometryRes.of(course.getGeometry());
        // 상세 정보를 내려주지 않는 구버전 추천 서버와도 동작하도록, 목록은 빈 목록으로 바꾼다
        this.elevationProfile = course.getElevationProfile() == null ? List.of()
                : course.getElevationProfile().stream().map(CourseElevationPointRes::of).toList();
        this.estimatedTimeMin = course.getEstimatedTimeMin();
        this.stairsCount = course.getStairsCount();
        this.hrZone = course.getHrZone();
        this.hrBpmRange = course.getHrBpmRange();
        this.biggestClimb = course.getBiggestClimb() == null ? null
                : CourseClimbRes.of(course.getBiggestClimb());
        this.slopeSegments = course.getSlopeSegments() == null ? List.of()
                : course.getSlopeSegments().stream().map(CourseSlopeSegmentRes::of).toList();
    }

    public static CourseRes of(AiRecommendRes.Course course) {
        return new CourseRes(course);
    }
}
